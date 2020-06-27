//
// Created by lenovo on 2020/4/25.
//

#ifndef MYMUSIC_WLQUEUE_H
#define MYMUSIC_WLQUEUE_H

#include "queue"
#include "pthread.h"
#include "androidLog.h"
#include "WlPlaystatus.h"

extern "C"{
#include "libavcodec/avcodec.h"
};

class WlQueue {

public:
    std::queue<AVPacket *> queuePacket;
    pthread_mutex_t mutexPacket;
    pthread_cond_t condPacket;

    WlPlaystatus *playstatus = NULL;

public:
    WlQueue(WlPlaystatus *playstatus);
    ~WlQueue();

    int putAvpacket(AVPacket *packet);
    int getAvpacket(AVPacket *packet);

    int getQueueSize();
    void clearAvpacket();

    void noticeQueue();
};


#endif //MYMUSIC_WLQUEUE_H
