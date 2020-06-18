//
// Created by lenovo on 2020/5/22.
//

#ifndef WLLIVEPUSHER_MYAUDIO_H
#define WLLIVEPUSHER_MYAUDIO_H

#include "jni.h"
#include "string"
#include "WlCallJava.h"
#include "RecordBuffer.h"
#include "AndroidLog.h"

extern "C"
{
#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>
}

class MyAudio {

public:
    WlCallJava *callJava = NULL;
    const char *path = NULL;
    SLObjectItf slObjectEngine = NULL;
    SLEngineItf  engineItf = NULL;

    SLObjectItf  recordObj = NULL;
    SLRecordItf  recordItf = NULL;

    SLAndroidSimpleBufferQueueItf recorderBufferQueue = NULL;

    RecordBuffer *recordBuffer;

    FILE *pcmFile = NULL;


    bool finish = false;
public:
    MyAudio(const char *path, WlCallJava *callJava);
    ~MyAudio();

    void startRecord();
    void stopRecord();
};


#endif //WLLIVEPUSHER_MYAUDIO_H
