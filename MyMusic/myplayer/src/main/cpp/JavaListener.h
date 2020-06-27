//
// Created by lenovo on 2020/4/23.
//
#include "jni.h"

#ifndef MYMUSIC_JAVALISTENER_H
#define MYMUSIC_JAVALISTENER_H


class JavaListener {

public:

    JavaVM *jvm;
    _JNIEnv *jniEnv;
    jobject jobj;

    jmethodID jmid;

public:
    JavaListener(JavaVM *vm, _JNIEnv *env, jobject obj);
    ~JavaListener();

    void onError(int threadType, int code, const char *msg);


};


#endif //MYMUSIC_JAVALISTENER_H
