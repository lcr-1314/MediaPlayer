//
// Created by lenovo on 2020/4/25.
//

#ifndef MYMUSIC_WLAUDIO_H
#define MYMUSIC_WLAUDIO_H

#include "WlQueue.h"
#include "WlPlaystatus.h"
#include "WlCallJava.h"

extern "C"
{
#include <libavutil/time.h>
#include "libavcodec/avcodec.h"
#include <libswresample/swresample.h>
#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>
};

class WlAudio {

public:
    int streamIndex = -1;
    AVCodecContext *avCodecContext = NULL;
    AVCodecParameters *codecpar = NULL;
    WlQueue *queue = NULL;
    WlPlaystatus *playstatus = NULL;
    WlCallJava *callJava = NULL;

    pthread_t thread_play;

    AVPacket *avPacket = NULL;

    AVFrame *avFrame = NULL;

    int ret = -1;

    uint8_t *buffer = NULL;
    int data_size = -1;
    int sample_rate = 0;

    int duration = 0;
    AVRational time_base;
    
    double clock = 0;
	double now_time = 0;
    double last_time = 0;

    int volumePercent = 100;

    int mute = 2;

    SLObjectItf engineObject = NULL;
    SLEngineItf engineEngine = NULL;

    SLObjectItf outputMixObject = NULL;
    SLEnvironmentalReverbItf outputMixEnironmentalReverb = NULL;
    SLEnvironmentalReverbSettings reverbSettings = SL_I3DL2_ENVIRONMENT_PRESET_STONECORRIDOR;

    //pcm
    SLObjectItf pcmPlayerObject = NULL;
    SLPlayItf pcmPlayerPlay = NULL;
    SLVolumeItf pcmPlayerVolume = NULL;
    SLMuteSoloItf pcmMutePlay = NULL;

    //缓冲器队列接口
    SLAndroidSimpleBufferQueueItf pcmBufferQueue;

    pthread_mutex_t codecMutex;

public:
    WlAudio(WlPlaystatus *playstatus, int sample_rate, WlCallJava *callJava);
    ~WlAudio();

    void play();

    int resampleAudio();

    void initOpenSLES();

    int getCurrentSampleRateForOpensles(int sample_rate);

    void pause();

    void resume();

    void stop();

    void release();

    void setVolume(int percent);

    void setMute(int mute);
};


#endif //MYMUSIC_WLAUDIO_H
