//
// Created by yangw on 2018-9-14.
//

#include <cwchar>
#include "jni.h"

#ifndef WLLIVEPUSHER_WLCALLJAVA_H
#define WLLIVEPUSHER_WLCALLJAVA_H

#define WL_THREAD_MAIN 1
#define WL_THREAD_CHILD 2

class WlCallJava {

public:

    JNIEnv *jniEnv = NULL;
    JavaVM *javaVM = NULL;
    jobject jobj;

    jmethodID jmid_connecting;
    jmethodID jmid_connectsuccess;
    jmethodID jmid_connectfail;
    jmethodID jmid_audiodata;

public:
    WlCallJava(JavaVM *javaVM, JNIEnv *jniEnv, jobject *jobj);
    ~WlCallJava();

    void onConnectint(int type);

    void onConnectsuccess();

    void onConnectFail(char *msg);

    void onAudioData(int type, void *packetdata, int size);









};


#endif //WLLIVEPUSHER_WLCALLJAVA_H
