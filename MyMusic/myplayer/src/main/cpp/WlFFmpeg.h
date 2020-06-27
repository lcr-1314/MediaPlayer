//
// Created by lenovo on 2020/4/25.
//

#ifndef MYMUSIC_WLFFMPEG_H
#define MYMUSIC_WLFFMPEG_H


#include "WlCallJava.h"
#include "pthread.h"
#include "WlAudio.h"
#include "WlVideo.h"
//#include "WlPlaystatus.h"

extern "C" {
#include <libavformat/avformat.h>
#include <libavutil/time.h>
};

class WlFFmpeg {

public:
    WlCallJava *callJava = NULL;
    const char *url = NULL;
    pthread_t decodeThread;

    AVFormatContext * pFormatCtx = NULL;
    WlAudio *audio = NULL;
    WlVideo *video = NULL;
    WlPlaystatus *playstatus = NULL;

    pthread_mutex_t init_mutex;

    bool exit = false;

    int duration = 0;
    pthread_mutex_t seek_mutex;

    bool supportMediacodec = false;

    const AVBitStreamFilter *bsFilter = NULL;

public:
    WlFFmpeg(WlPlaystatus *playstatus, WlCallJava *callJava, const char *url);
    ~WlFFmpeg();

    void parpared();
    void decodeFFmpegThread();

    void start();

    void pause();

    void resume();

    void release();

    void seek(int64_t seds);

    void setVolume(int percent);

    void setMute(int mute);

    int getCodeContext(AVCodecParameters *codecpar, AVCodecContext **avCodecContext);
};


#endif //MYMUSIC_WLFFMPEG_H
