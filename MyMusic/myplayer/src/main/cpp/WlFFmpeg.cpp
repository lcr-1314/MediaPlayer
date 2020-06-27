//
// Created by lenovo on 2020/4/25.
//
#include "WlFFmpeg.h"

WlFFmpeg::WlFFmpeg(WlPlaystatus *playstatus, WlCallJava *callJava, const char *url) {
    this->playstatus = playstatus;
    this->callJava = callJava;
    this->url = url;
    exit = false;

    // 初始化锁
    pthread_mutex_init(&init_mutex, NULL);
    pthread_mutex_init(&seek_mutex, NULL);
}

void *decodeFFmpeg(void *data) {
    WlFFmpeg *wlFFmpeg = (WlFFmpeg *) data;
    wlFFmpeg->decodeFFmpegThread(); // 调用解码函数进行解码
    return 0;
}

void WlFFmpeg::parpared() {
    // 创建一个线程，并在线程的回调函数中调用解码函数进行解码
    pthread_create(&decodeThread, NULL, decodeFFmpeg, this);
}

int avformat_callback(void *ctx) {
    WlFFmpeg *fFmpeg = (WlFFmpeg *) ctx;
    if (fFmpeg->playstatus->exit) { // 如果已经退出
        return AVERROR_EOF;         // 返回错误，使读取输入流时不阻塞，可正常退出
    }

    return 0;
}

void WlFFmpeg::decodeFFmpegThread() {
    pthread_mutex_lock(&init_mutex); // 加锁保护线程
    av_register_all();
    avformat_network_init(); // 注册网络

    pFormatCtx = avformat_alloc_context(); // 获取解码格式上下文

    pFormatCtx->interrupt_callback.callback = avformat_callback; // 添加回调，防止阻塞
    pFormatCtx->interrupt_callback.opaque = this;   // 参数

    // 打开输入流
    if (avformat_open_input(&pFormatCtx, url, NULL, NULL) != 0) {
        if (LOG_DEBUG) {
            LOGE("can not open url :%s", url);
        }

        // 打开输入流失败
        callJava->onCallError(CHILD_THREAD, 1001, "can not open url");  //调用java函数，通知java层
        exit = true;                        // 将标志位置为退出状态
        pthread_mutex_unlock(&init_mutex);  // 则解锁
        return;
    }

    // 获取输入流信息  // 放到解码格式上下文
    if (avformat_find_stream_info(pFormatCtx, NULL) < 0) {
        if (LOG_DEBUG) {
            LOGE("can not find stream from url:%s", url);
        }

        // 获取输入流信息失败
        callJava->onCallError(CHILD_THREAD, 1002, "can not find stream from url"); //调用java函数，通知java层
        exit = true;                        // 将标志位置为退出状态
        pthread_mutex_unlock(&init_mutex);  // 则解锁

        return;
    }

    // 循环找流
    for (int i = 0; i < pFormatCtx->nb_streams; i++) {
        if (pFormatCtx->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_AUDIO) { // 音频流
            if (audio == NULL) {
                // 初始化音频流的类
                audio = new WlAudio(playstatus, pFormatCtx->streams[i]->codecpar->sample_rate, callJava);
                audio->streamIndex = i;     // 音频流的索引
                audio->codecpar = pFormatCtx->streams[i]->codecpar;         // 音频流解码器的参数
                audio->duration = pFormatCtx->duration / AV_TIME_BASE;      // 播放时长
                audio->time_base = pFormatCtx->streams[i]->time_base;       // 帧时间戳的基本时间单位
                duration = audio->duration;                                 // 播放时长
            }
        } else if (pFormatCtx->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_VIDEO) { // 视频流
            if (video == NULL) {
                video = new WlVideo(playstatus, callJava);
                video->streamIndex = i;     // 视频流的索引
                video->codecpar = pFormatCtx->streams[i]->codecpar;         // 视频流解码器的参数
                video->time_base = pFormatCtx->streams[i]->time_base;       // 帧时间戳的基本时间单位

                int time = pFormatCtx->duration / AV_TIME_BASE;             // 播放时长

                // 平均帧速率
                int num = pFormatCtx->streams[i]->avg_frame_rate.num;
                int den = pFormatCtx->streams[i]->avg_frame_rate.den;
                if (num != 0 && den != 0) {
                    int fps = num / den;      // [25/1]
                    video->defautDelayTime = 1.0 / fps;  // 计算出一秒播放多少帧
                }
            }
        }
    }

    // 获取各自对应的解码器
    if (audio != NULL) {
        // 获取音频流解码器上下文，并打开对应解码器
        getCodeContext(audio->codecpar, &audio->avCodecContext);
    }

    if (video != NULL) {
        // 获取视频流解码器上下文，并打开对应解码器
        getCodeContext(video->codecpar, &video->avCodecContext);
    }

    if (callJava != NULL) {
        if (playstatus != NULL && !playstatus->exit) {
            callJava->onCallParpared(CHILD_THREAD); // 通知java层，已经准备好
        } else {
            exit = true;                            // 如果已经退出，则将标志位置为退出状态 ???
            //是否需要设置为已播放完成，或者设置为播放下一曲？
        }
    }
    pthread_mutex_unlock(&init_mutex);              // 解保护锁
}

void WlFFmpeg::start() {
    if (audio == NULL && video == NULL) {
        if (audio == NULL) {
            if (LOG_DEBUG) {
                LOGE("audio is null");
            }
            callJava->onCallError(CHILD_THREAD, 1007, "audio is null");
        }
        if (video == NULL) {
            if (LOG_DEBUG) {
                LOGE("audio is null");
            }
            callJava->onCallError(CHILD_THREAD, 1008, "video is null");
        }

        return;
    }

    const char *codecName;
    if (video != NULL) {
        supportMediacodec = false;
        if (audio != NULL) {
            video->audio = audio;                         // 用以计算播放时差，修正同步播放
        }
        codecName = (video->avCodecContext->codec)->name; // 硬解码设备名称 // 通过解码器上下文获取得到
        LOGE("=============codecName:%s", codecName);
        // 判断是否有对应支持当前视频的硬解码
        if (supportMediacodec = callJava->onCallIsSupportVideo(codecName)) {
            if (strcasecmp(codecName, "h264") == 0) {
                // 找到相应解码器的过滤器
                bsFilter = av_bsf_get_by_name("h264_mp4toannexb");
            } else if (strcasecmp(codecName, "h265") == 0) {
                bsFilter = av_bsf_get_by_name("hevc_mp4toannexb");
            } else if(strcasecmp(codecName, "rv40") == 0){
                bsFilter = av_bsf_get_by_name("hevc_mp4toannexb");
            }

            if (bsFilter == NULL) {
                supportMediacodec = false;
                goto end;
            }

            // 初始化过滤器上下文
            if (av_bsf_alloc(bsFilter, &video->abs_ctx) != 0) {
                supportMediacodec = false;
                goto end;
            }

            // 添加解码器属性
            if (avcodec_parameters_copy(video->abs_ctx->par_in, video->codecpar) < 0) {
                supportMediacodec = false;
                av_bsf_free(&video->abs_ctx);
                video->abs_ctx = NULL;
                goto end;
            }

            // 初始化过滤器上下文
            if (av_bsf_init(video->abs_ctx) != 0) {
                supportMediacodec = false;
                av_bsf_free(&video->abs_ctx);
                video->abs_ctx = NULL;
                goto end;
            }
            video->abs_ctx->time_base_in = video->time_base;
        }
    }

    end:
    LOGE("=============supportMediacodec:%d", supportMediacodec);
    if (video != NULL && supportMediacodec) {
        video->codectype = CODEC_MEDIACODEC;     // 置为支持硬解码当前视频
        video->wlCallJava->onCallInitMediacodec( // 初始化 MediaCodec 的 MediaFormat.
                codecName,
                video->avCodecContext->width,
                video->avCodecContext->height,
                video->avCodecContext->extradata_size,
                video->avCodecContext->extradata_size,
                video->avCodecContext->extradata,
                video->avCodecContext->extradata);
    }

    if (audio != NULL) {
        audio->play();  // 开始播放音频流
    }
    if (video != NULL) {
        video->play();  // 开始播放视频流
    }

    while (playstatus != NULL && !playstatus->exit) {
        if (playstatus->seek) {
            av_usleep(1000 * 100);
            continue;
        }

        // 以音频线性播放
        if (audio != NULL && audio->queue->getQueueSize() > 40) { // 缓冲队列
            av_usleep(1000 * 100);
            continue;
        }
        AVPacket *avPacket = av_packet_alloc();
        pthread_mutex_lock(&seek_mutex);    // 加锁
        int ret = av_read_frame(pFormatCtx, avPacket); // 读取到avPacket数据
        pthread_mutex_unlock(&seek_mutex);  // 解锁
        if (ret == 0) {
            if (audio != NULL &&avPacket->stream_index == audio->streamIndex) {
                audio->queue->putAvpacket(avPacket); // 入队列
            } else if (video != NULL && avPacket->stream_index == video->streamIndex) {
                video->queue->putAvpacket(avPacket); // 视频流入队列
                // LOGD("=======获取到视频AvPacket.");
            } else {
                av_packet_free(&avPacket);
                av_free(avPacket);
                avPacket = NULL;
            }
        } else {
            if (LOG_DEBUG) {
                LOGE("decode finished");
            }
            av_packet_free(&avPacket);
            av_free(avPacket);
            avPacket = NULL;

            while (playstatus != NULL && !playstatus->exit) {
                if (audio != NULL && audio->queue->getQueueSize() > 0) {
                    av_usleep(1000 * 100);
                    continue;
                } else {
                    if(!playstatus->seek){
                        av_usleep(1000 * 100);
                        playstatus->exit = true;
                    }
                    break;
                }
            }
            break;
        }
    }

    if (callJava != NULL) {
        callJava->onComplete(CHILD_THREAD);
    }
    exit = true;

    if (LOG_DEBUG) {
        LOGD("解码完成.");
    }
}

void WlFFmpeg::pause() {
    if (playstatus != NULL) {
        playstatus->pause = true;
    }

    if (audio != NULL) {
        audio->pause();
    }
}

void WlFFmpeg::resume() {
    if (playstatus != NULL) {
        playstatus->pause = false;
    }

    if (audio != NULL) {
        audio->resume();
    }
}

void WlFFmpeg::release() {
    playstatus->exit = true;

    pthread_join(decodeThread, NULL);

    pthread_mutex_lock(&init_mutex);

    int sleepCount = 0;
    while (!exit) {
        if (sleepCount > 1000) {
            exit = true;
        }

        if (LOG_DEBUG) {
            LOGE("wait ffmpeg exit %d", sleepCount);
        }
        sleepCount++;
        av_usleep(1000 * 10);//
    }

    if (LOG_DEBUG) {
        LOGE("释放 Audio");
    }

    if (audio != NULL) {
        audio->release();
        delete (audio);
        audio = NULL;
    }

    if (LOG_DEBUG) {
        LOGE("释放 video");
    }

    if (video != NULL) {
        video->release();
        delete (video);
        video = NULL;
    }

    if (LOG_DEBUG) {
        LOGE("释放 封装格式上下文");
    }

    if (pFormatCtx != NULL) {
        avformat_close_input(&pFormatCtx);
        avformat_free_context(pFormatCtx);
        pFormatCtx = NULL;
    }

    if (LOG_DEBUG) {
        LOGE("释放 callJava");
    }

    if (callJava != NULL) {
        callJava = NULL;
    }

    if (LOG_DEBUG) {
        LOGE("释放 playstatus");
    }

    if (playstatus != NULL) {
        playstatus = NULL;
    }
    pthread_mutex_unlock(&init_mutex);
}

WlFFmpeg::~WlFFmpeg() {
    pthread_mutex_destroy(&init_mutex);
    pthread_mutex_destroy(&seek_mutex);
}

void WlFFmpeg::seek(int64_t seds) {
    if (duration <= 0) {
        return;
    }

    if (seds >= 0 && seds <= duration) {
        playstatus->seek = true;
        pthread_mutex_lock(&seek_mutex);
        int64_t rel = seds * AV_TIME_BASE;
        avformat_seek_file(pFormatCtx, -1, INT64_MIN, rel, INT64_MAX, 0); // 定位播放
        if (audio != NULL) {
            audio->queue->clearAvpacket();
            audio->clock = 0;
            audio->last_time = 0;
            pthread_mutex_lock(&audio->codecMutex);
            avcodec_flush_buffers(audio->avCodecContext);
            pthread_mutex_unlock(&audio->codecMutex);
        }
        if(video != NULL){
            video->queue->clearAvpacket();
            video->clock = 0;
            pthread_mutex_lock(&video->codecMutex);
            avcodec_flush_buffers(video->avCodecContext);
            pthread_mutex_unlock(&video->codecMutex);
        }
        pthread_mutex_unlock(&seek_mutex);
        playstatus->seek = false;
    }
}

void WlFFmpeg::setVolume(int percent) {
    if (audio != NULL) {
        audio->setVolume(percent);
    }
}

void WlFFmpeg::setMute(int mute) {
    if (audio != NULL) {
        audio->setMute(mute);
    }
}

int WlFFmpeg::getCodeContext(AVCodecParameters *codecpar, AVCodecContext **avCodecContext) {
    AVCodec *dec = avcodec_find_decoder(codecpar->codec_id); // 通过对应解码器id查找解码器
    if (!dec) {
        if (LOG_DEBUG) {
            LOGE("can not find decoder");
        }

        callJava->onCallError(CHILD_THREAD, 1003, "can not find decoder"); // 通知java层，查找不到解码器
        exit = true;                        // 设置为退出状态
        pthread_mutex_unlock(&init_mutex);  // 解保护锁
        return -1;
    }

    // 解码器上下文
    *avCodecContext = avcodec_alloc_context3(dec);
    if (!*avCodecContext) {
        if (LOG_DEBUG) {
            LOGE("can not alloc new decoderctx");
        }

        callJava->onCallError(CHILD_THREAD, 1004, "can not alloc new decoderctx"); // 通知java层
        exit = true;                        // 设置为退出状态
        pthread_mutex_unlock(&init_mutex);  // 解保护锁
        return -1;
    }

    // 将解码器属性复制到解码器上下文中
    if (avcodec_parameters_to_context(*avCodecContext, codecpar) < 0) {
        if (LOG_DEBUG) {
            LOGE("can not fill decoderctx");
        }

        callJava->onCallError(CHILD_THREAD, 1005, "can not fill decoderctx"); // 通知java层
        exit = true;                        // 设置为退出状态
        pthread_mutex_unlock(&init_mutex);  // 解保护锁
        return -1;
    }

    // 打开解码器
    if (avcodec_open2(*avCodecContext, dec, 0) != 0) {
        if (LOG_DEBUG) {
            LOGE("can not open audio streams");
        }

        callJava->onCallError(CHILD_THREAD, 1006, "can not open audio streams"); // 通知java层
        exit = true;                        // 设置为退出状态
        pthread_mutex_unlock(&init_mutex);  // 解保护锁
        return -1;
    }

    return 0;
}

















