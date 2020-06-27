//
// Created by lenovo on 2020/4/25.
//

#include "WlAudio.h"

WlAudio::WlAudio(WlPlaystatus *playstatus, int sample_rate, WlCallJava *callJava) {
    this->callJava = callJava;
    this->playstatus = playstatus;
    this->sample_rate = sample_rate;
    queue = new WlQueue(playstatus);
    buffer = (uint8_t *) av_malloc(sample_rate * 2 * 2);
    pthread_mutex_init(&codecMutex, NULL);
}

WlAudio::~WlAudio() {
    pthread_mutex_destroy(&codecMutex);
}

void *decodPlay(void *data) {
    WlAudio *wlAudio = (WlAudio *) data;
    wlAudio->initOpenSLES(); // 初始化opensl es

    return 0;
}

void WlAudio::play() {
    if (playstatus != NULL && !playstatus->exit) {
        pthread_create(&thread_play, NULL, decodPlay, this); // 创建线程，在线程中创建opensl es
    }
}

// 重采样
int WlAudio::resampleAudio() {
    data_size = 0;
    while (playstatus != NULL && !playstatus->exit) {   // 未退出，则一直循环获取 AVPacket.
        if (playstatus->seek) {
            av_usleep(1000 * 100);  // 微秒级睡眠
            continue;
        }

        if (queue->getQueueSize() == 0) { // 判断队列是否有数据，为0则没有数据
            if (!playstatus->load) {
                playstatus->load = true;
                callJava->onCallLoad(CHILD_THREAD, true);
            }

            av_usleep(1000 * 100);
            continue;
        } else {
            if (playstatus->load) {
                playstatus->load = false;
                callJava->onCallLoad(CHILD_THREAD, false);
            }
        }

        avPacket = av_packet_alloc();
        if (queue->getAvpacket(avPacket) != 0) { // 出队列 // 得到对应的 AVPacket
            av_packet_free(&avPacket);
            av_free(avPacket);
            avPacket = NULL;
            continue;
        }

        pthread_mutex_lock(&codecMutex);        // 加锁
        ret = avcodec_send_packet(avCodecContext, avPacket);    // 向解码器发送数据avPacket
        if (ret != 0) {
            av_packet_free(&avPacket);
            av_free(avPacket);
            avPacket = NULL;
            pthread_mutex_unlock(&codecMutex);  // 解锁
            continue;
        }

        avFrame = av_frame_alloc();
        ret = avcodec_receive_frame(avCodecContext, avFrame);   // 从解码器接收数据到avFrame
        if (ret == 0) {
            // 重置声道数或声道布局
            if (avFrame->channels > 0 && avFrame->channel_layout == 0) {
                avFrame->channel_layout = av_get_default_channel_layout(
                        avFrame->channels);         // 获取声道布局
            } else if (avFrame->channels == 0 && avFrame->channel_layout > 0) {
                avFrame->channels = av_get_channel_layout_nb_channels(
                        avFrame->channel_layout);   // 通过声道布局获取声道
            }

            SwrContext *swr_ctx = NULL;
            swr_ctx = swr_alloc_set_opts(
                    NULL,
                    AV_CH_LAYOUT_STEREO,
                    AV_SAMPLE_FMT_S16,
                    avFrame->sample_rate,
                    avFrame->channel_layout,
                    (AVSampleFormat) (avFrame->format),
                    avFrame->sample_rate,
                    NULL, NULL);

            // 初始化转码上下文 swr_ctx
            if (!swr_ctx || swr_init(swr_ctx) < 0) {
                av_packet_free(&avPacket);
                av_free(avPacket);
                avPacket = NULL;

                av_frame_free(&avFrame);
                av_free(avFrame);
                avFrame = NULL;

                if (swr_ctx != NULL) {
                    swr_free(&swr_ctx);
                    swr_ctx = NULL;
                }
                pthread_mutex_unlock(&codecMutex);  // 解锁
                continue;
            }

            // 转换到buffer里
            int nb = swr_convert(
                    swr_ctx,
                    &buffer,
                    avFrame->nb_samples,
                    (const uint8_t **) (avFrame->data),
                    avFrame->nb_samples);

            int out_channels = av_get_channel_layout_nb_channels(AV_CH_LAYOUT_STEREO);
            data_size = nb * out_channels * av_get_bytes_per_sample(AV_SAMPLE_FMT_S16);

            now_time = avFrame->pts * av_q2d(time_base);    // 当前播放时间
            if (now_time < clock) {
                now_time = clock;
            }
            clock = now_time;   // 当前时间

            av_packet_free(&avPacket);
            av_free(avPacket);
            avPacket = NULL;

            av_frame_free(&avFrame);
            av_free(avFrame);
            avFrame = NULL;

            swr_free(&swr_ctx);
            swr_ctx = NULL;
            pthread_mutex_unlock(&codecMutex);  // 解锁
            break;
        } else {
            av_packet_free(&avPacket);
            av_free(avPacket);
            avPacket = NULL;

            av_frame_free(&avFrame);
            av_free(avFrame);
            avFrame = NULL;
            pthread_mutex_unlock(&codecMutex);  // 解锁
            continue;
        }
    }

    return data_size;
}

void pcmBufferCallBack(SLAndroidSimpleBufferQueueItf bf, void *context) {
    WlAudio *wlAudio = (WlAudio *) (context);
    if (wlAudio != NULL) {
        int buffersize = wlAudio->resampleAudio(); // 重采样
        // LOGE("buffersize:%d", buffersize);
        if (buffersize > 0) {
            wlAudio->clock += buffersize / ((double) wlAudio->sample_rate * 2 * 2);

            if (wlAudio->clock - wlAudio->last_time >= 0.1) {
                wlAudio->last_time = wlAudio->clock;
                wlAudio->callJava->onCallTimeInfo(CHILD_THREAD,
                                                  wlAudio->clock,
                                                  wlAudio->duration); // 通知java层，当前时间和总时间
            }
            (*wlAudio->pcmBufferQueue)->Enqueue(wlAudio->pcmBufferQueue,
                                               (char *) wlAudio->buffer, buffersize);
        }
    }
}

void WlAudio::initOpenSLES() {
    // 创建引擎
    slCreateEngine(&engineObject, 0, 0, 0, 0, 0);
    (*engineObject)->Realize(engineObject, SL_BOOLEAN_FALSE);
    (*engineObject)->GetInterface(engineObject, SL_IID_ENGINE, &engineEngine);

    //创建并设置混音器
    const SLInterfaceID mids[1] = {SL_IID_ENVIRONMENTALREVERB};
    const SLboolean mreq[1] = {SL_BOOLEAN_FALSE};
    // 创建混音器
    (*engineEngine)->CreateOutputMix(engineEngine, &outputMixObject, 1, mids, mreq);
    (*outputMixObject)->Realize(outputMixObject, SL_BOOLEAN_FALSE);
    (*outputMixObject)->GetInterface(outputMixObject, SL_IID_ENVIRONMENTALREVERB,
                                     &outputMixEnironmentalReverb);
    // 设置混音器环境变量
    (*outputMixEnironmentalReverb)->SetEnvironmentalReverbProperties(outputMixEnironmentalReverb,
                                                                     &reverbSettings);

    // 设置要创建播放器的参数
    SLDataLocator_OutputMix outputMix = {SL_DATALOCATOR_OUTPUTMIX, outputMixObject};
    SLDataSink audioSnk = {&outputMix, 0};
    SLDataLocator_AndroidSimpleBufferQueue android_queue = {SL_DATALOCATOR_ANDROIDSIMPLEBUFFERQUEUE, 2};
    SLDataFormat_PCM pcm = {
            SL_DATAFORMAT_PCM,
            2,                                              // 立体声
            getCurrentSampleRateForOpensles(sample_rate),   // 获取当前采样率
            SL_PCMSAMPLEFORMAT_FIXED_16,
            SL_PCMSAMPLEFORMAT_FIXED_16,
            SL_SPEAKER_FRONT_LEFT | SL_SPEAKER_FRONT_RIGHT, // 左右声道
            SL_BYTEORDER_LITTLEENDIAN
    };
    SLDataSource slDataSource = {&android_queue, &pcm};

    const SLInterfaceID ids[4] = {SL_IID_BUFFERQUEUE, SL_IID_VOLUME, SL_IID_PLAYBACKRATE, SL_IID_MUTESOLO};
    const SLboolean req[4] = {SL_BOOLEAN_TRUE, SL_BOOLEAN_TRUE, SL_BOOLEAN_TRUE, SL_BOOLEAN_TRUE};

    // 创建播放器
    (*engineEngine)->CreateAudioPlayer(engineEngine, &pcmPlayerObject,
                                         &slDataSource, &audioSnk, 4, ids, req);
    (*pcmPlayerObject)->Realize(pcmPlayerObject, SL_BOOLEAN_FALSE);
    (*pcmPlayerObject)->GetInterface(pcmPlayerObject, SL_IID_PLAY, &pcmPlayerPlay);

    // 获取声道队列
    (*pcmPlayerObject)->GetInterface(pcmPlayerObject, SL_IID_BUFFERQUEUE, &pcmBufferQueue);
    // 注册声道队列回调函数
    (*pcmBufferQueue)->RegisterCallback(pcmBufferQueue, pcmBufferCallBack, this);

    // 获取声音接口
    (*pcmPlayerObject)->GetInterface(pcmPlayerObject, SL_IID_VOLUME, &pcmPlayerVolume);
    setVolume(volumePercent);

    // 左右声道及立体声
    (*pcmPlayerObject)->GetInterface(pcmPlayerObject, SL_IID_MUTESOLO, &pcmMutePlay);

    // 设置为播放状态
    (*pcmPlayerPlay)->SetPlayState(pcmPlayerPlay, SL_PLAYSTATE_PLAYING);

    //回调声道队列函数
    pcmBufferCallBack(pcmBufferQueue, this);
}

int WlAudio::getCurrentSampleRateForOpensles(int sample_rate) {
    int rate = 0;
    switch (sample_rate) {
        case 8000:
            rate = SL_SAMPLINGRATE_8;
            break;
        case 11025:
            rate = SL_SAMPLINGRATE_11_025;
            break;
        case 12000:
            rate = SL_SAMPLINGRATE_12;
            break;
        case 16000:
            rate = SL_SAMPLINGRATE_16;
            break;
        case 22050:
            rate = SL_SAMPLINGRATE_22_05;
            break;
        case 24000:
            rate = SL_SAMPLINGRATE_24;
            break;
        case 32000:
            rate = SL_SAMPLINGRATE_32;
            break;
        case 44100:
            rate = SL_SAMPLINGRATE_44_1;
            break;
        case 48000:
            rate = SL_SAMPLINGRATE_48;
            break;
        case 64000:
            rate = SL_SAMPLINGRATE_64;
            break;
        case 88200:
            rate = SL_SAMPLINGRATE_88_2;
            break;
        case 96000:
            rate = SL_SAMPLINGRATE_96;
            break;
        case 192000:
            rate = SL_SAMPLINGRATE_192;
            break;
        default:
            rate = SL_SAMPLINGRATE_44_1;
            break;
    }

    return rate;
}

void WlAudio::pause() {
    if (pcmPlayerPlay != NULL) {
        (*pcmPlayerPlay)->SetPlayState(pcmPlayerPlay, SL_PLAYSTATE_PAUSED);
    }

}

void WlAudio::resume() {
    if (pcmPlayerPlay != NULL) {
        (*pcmPlayerPlay)->SetPlayState(pcmPlayerPlay, SL_PLAYSTATE_PLAYING);
    }
}

void WlAudio::release() {

    if (queue != NULL) {
        queue->noticeQueue();
    }
    pthread_join(thread_play, NULL);

    if (queue != NULL) {
        delete (queue);
        queue = NULL;
    }

    if (pcmPlayerObject != NULL) {
        (*pcmPlayerObject)->Destroy(pcmPlayerObject);
        pcmPlayerObject = NULL;

        pcmPlayerPlay = NULL;
        pcmBufferQueue = NULL;
        pcmMutePlay = NULL;
        pcmPlayerVolume = NULL;
    }

    if (outputMixObject != NULL) {
        (*outputMixObject)->Destroy(outputMixObject);
        outputMixObject = NULL;
        outputMixEnironmentalReverb = NULL;
    }

    if (engineObject != NULL) {
        (*engineObject)->Destroy(engineObject);
        engineObject = NULL;
        engineEngine = NULL;
    }

    if (buffer != NULL) {
        free(buffer);
        buffer = NULL;
    }

    if (avCodecContext != NULL) {
        avcodec_close(avCodecContext);
        avcodec_free_context(&avCodecContext);
        avCodecContext = NULL;
    }

    if (playstatus != NULL) {
        playstatus = NULL;
    }

    if (callJava != NULL) {
        callJava = NULL;
    }
}

void WlAudio::stop() {
    if (pcmPlayerPlay != NULL) {
        (*pcmPlayerPlay)->SetPlayState(pcmPlayerPlay, SL_PLAYSTATE_STOPPED);
    }
}

void WlAudio::setVolume(int percent) {
    volumePercent = percent;
    if (pcmPlayerVolume != NULL) {
        if (percent > 30) {
            (*pcmPlayerVolume)->SetVolumeLevel(pcmPlayerVolume, (100 - percent) * -20);
        } else if (percent > 25) {
            (*pcmPlayerVolume)->SetVolumeLevel(pcmPlayerVolume, (100 - percent) * -22);
        } else if (percent > 20) {
            (*pcmPlayerVolume)->SetVolumeLevel(pcmPlayerVolume, (100 - percent) * -25);
        } else if (percent > 15) {
            (*pcmPlayerVolume)->SetVolumeLevel(pcmPlayerVolume, (100 - percent) * -28);
        } else if (percent > 10) {
            (*pcmPlayerVolume)->SetVolumeLevel(pcmPlayerVolume, (100 - percent) * -30);
        } else if (percent > 5) {
            (*pcmPlayerVolume)->SetVolumeLevel(pcmPlayerVolume, (100 - percent) * -34);
        } else if (percent > 3) {
            (*pcmPlayerVolume)->SetVolumeLevel(pcmPlayerVolume, (100 - percent) * -37);
        } else if (percent > 0) {
            (*pcmPlayerVolume)->SetVolumeLevel(pcmPlayerVolume, (100 - percent) * -40);
        } else {
            (*pcmPlayerVolume)->SetVolumeLevel(pcmPlayerVolume, (100 - percent) * -100);
        }
    }
}

void WlAudio::setMute(int mute) {
    this->mute = mute;

    if (pcmMutePlay != NULL) {
        if (mute == 0) {        //right
            (*pcmMutePlay)->SetChannelMute(pcmMutePlay, 1, false);
            (*pcmMutePlay)->SetChannelMute(pcmMutePlay, 0, true);
        } else if (mute == 1) { //left
            (*pcmMutePlay)->SetChannelMute(pcmMutePlay, 0, false);
            (*pcmMutePlay)->SetChannelMute(pcmMutePlay, 1, true);
        } else {                //立体声
            (*pcmMutePlay)->SetChannelMute(pcmMutePlay, 0, false);
            (*pcmMutePlay)->SetChannelMute(pcmMutePlay, 1, false);
        }
    }
}