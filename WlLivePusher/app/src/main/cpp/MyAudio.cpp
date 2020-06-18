//
// Created by lenovo on 2020/5/22.
//

#include "MyAudio.h"

MyAudio::MyAudio(const char *_path, WlCallJava *callJava) {
    this->path = _path;
    this->callJava = callJava;

    pcmFile = fopen(path, "w");
    recordBuffer = new RecordBuffer(4096);
}

MyAudio::~MyAudio() {
    if (recordObj != NULL) {
        (*recordObj)->Destroy(recordObj);
        recordObj = NULL;

        recordItf = NULL;
        recorderBufferQueue = NULL;
    }

    if(recordBuffer != NULL){
        delete(recordBuffer);
        recordBuffer = NULL;
    }

    if (slObjectEngine != NULL) {
        (*slObjectEngine)->Destroy(slObjectEngine);
        slObjectEngine = NULL;
        engineItf = NULL;
    }
}

void bqRecorderCallback(SLAndroidSimpleBufferQueueItf bq, void *context) {
    MyAudio *audio = (MyAudio *)context;

    fwrite(audio->recordBuffer->getNowBuffer(), 1, 4096, audio->pcmFile);

     //将buffer返回就可以
    audio->callJava->onAudioData(WL_THREAD_CHILD, audio->recordBuffer->getNowBuffer(), 4096);

    if (audio->finish) {
        LOGE("录制完成");
        (*audio->recordItf)->SetRecordState(audio->recordItf, SL_RECORDSTATE_STOPPED);
        //
        (*audio->recordObj)->Destroy(audio->recordObj);
        audio->recordObj = NULL;
        audio->recordItf = NULL;

        (*audio->slObjectEngine)->Destroy(audio->slObjectEngine);
        audio->slObjectEngine = NULL;
        audio->engineItf = NULL;
        delete(audio->recordBuffer);

        // path
    } else {
        LOGE("正在录制");
        (*audio->recorderBufferQueue)->Enqueue(audio->recorderBufferQueue, audio->recordBuffer->getRecordBuffer(), 4096);
    }
}

void MyAudio::startRecord() {
    if (finish) {
        return;
    }

    finish = false;

    slCreateEngine(&slObjectEngine, 0, NULL, 0, NULL, NULL);
    (*slObjectEngine)->Realize(slObjectEngine, SL_BOOLEAN_FALSE);
    (*slObjectEngine)->GetInterface(slObjectEngine, SL_IID_ENGINE, &engineItf);

    SLDataLocator_IODevice loc_dev = {SL_DATALOCATOR_IODEVICE,
                                      SL_IODEVICE_AUDIOINPUT,
                                      SL_DEFAULTDEVICEID_AUDIOINPUT,
                                      NULL};
    SLDataSource audioSrc = {&loc_dev, NULL};

    SLDataLocator_AndroidSimpleBufferQueue loc_bq = {
            SL_DATALOCATOR_ANDROIDSIMPLEBUFFERQUEUE,
            2
    };

    SLDataFormat_PCM format_pcm = {
            SL_DATAFORMAT_PCM, 2, SL_SAMPLINGRATE_44_1,
            SL_PCMSAMPLEFORMAT_FIXED_16, SL_PCMSAMPLEFORMAT_FIXED_16,
            SL_SPEAKER_FRONT_LEFT | SL_SPEAKER_FRONT_RIGHT, SL_BYTEORDER_LITTLEENDIAN
    };

    SLDataSink audioSnk = {&loc_bq, &format_pcm};

    const SLInterfaceID id[1] = {SL_IID_ANDROIDSIMPLEBUFFERQUEUE};
    const SLboolean req[1] = {SL_BOOLEAN_TRUE};

    (*engineItf)->CreateAudioRecorder(engineItf, &recordObj, &audioSrc, &audioSnk, 1, id, req);
    (*recordObj)->Realize(recordObj, SL_BOOLEAN_FALSE);
    (*recordObj)->GetInterface(recordObj, SL_IID_RECORD, &recordItf);

    (*recordObj)->GetInterface(recordObj, SL_IID_ANDROIDSIMPLEBUFFERQUEUE, &recorderBufferQueue);

    (*recorderBufferQueue)->Enqueue(recorderBufferQueue, recordBuffer->getRecordBuffer(), 4096);

    (*recorderBufferQueue)->RegisterCallback(recorderBufferQueue, bqRecorderCallback, this);

    (*recordItf)->SetRecordState(recordItf, SL_RECORDSTATE_RECORDING);

}

void MyAudio::stopRecord() {
    (*recordItf)->SetRecordState(recordItf, SL_RECORDSTATE_STOPPED);

    finish = true;
    callJava = NULL;
}
