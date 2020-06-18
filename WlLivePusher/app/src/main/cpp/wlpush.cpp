#include <jni.h>
#include <string>

#include "AndroidLog.h"

#include "RtmpPush.h"
#include "WlCallJava.h"
#include "MyAudio.h"

//record
MyAudio *audio = NULL;

//live push
RtmpPush * rtmpPush = NULL;
WlCallJava *wlCallJava = NULL;
JavaVM *javaVM = NULL;
bool exit = true;

extern "C"
JNIEXPORT void JNICALL
Java_com_android_wllivepusher_push_WlPushVideo_stopRecord(JNIEnv *env, jobject thiz) {
    audio->stopRecord();

    delete(audio);
    audio = NULL;

    delete (wlCallJava);
    wlCallJava = NULL;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_android_wllivepusher_push_WlPushVideo_startRecord(JNIEnv *env, jobject thiz, jstring _path) {
    const char *path = env->GetStringUTFChars(_path, 0);
    if (wlCallJava == NULL) {
        wlCallJava = new WlCallJava(javaVM, env, &thiz);
    }

    if (audio == NULL) {
        audio = new MyAudio(path, wlCallJava);
        audio->startRecord();
    }

    env->ReleaseStringUTFChars(_path, path);
}

// push
extern "C"
JNIEXPORT void JNICALL
Java_com_android_wllivepusher_push_WlPushVideo_initPush(JNIEnv *env, jobject instance,
                                                        jstring pushUrl_) {
    const char *pushUrl = env->GetStringUTFChars(pushUrl_, 0);

    if (wlCallJava == NULL) {
        exit = false;
        wlCallJava = new WlCallJava(javaVM, env, &instance);
    }
    rtmpPush = new RtmpPush(pushUrl, wlCallJava);
    rtmpPush->init();

    env->ReleaseStringUTFChars(pushUrl_, pushUrl);
}

extern "C"
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved)
{
    javaVM = vm;
    JNIEnv* env;
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_4) != JNI_OK) {
        if (LOG_SHOW) {
            LOGE("GetEnv failed!");
        }
        return -1;
    }
    return JNI_VERSION_1_4;
}

extern "C"
JNIEXPORT void JNICALL JNI_OnUnload(JavaVM* vm, void* reserved){
    javaVM = NULL;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_android_wllivepusher_push_WlPushVideo_pushSPSPPS(JNIEnv *env, jobject instance,
                                                          jbyteArray sps_, jint sps_len,
                                                          jbyteArray pps_, jint pps_len) {
    jbyte *sps = env->GetByteArrayElements(sps_, NULL);
    jbyte *pps = env->GetByteArrayElements(pps_, NULL);

    if (rtmpPush != NULL && !exit) {
        rtmpPush->pushSPSPPS(reinterpret_cast<char *>(sps), sps_len, reinterpret_cast<char *>(pps),
                             pps_len);
    }

    env->ReleaseByteArrayElements(sps_, sps, 0);
    env->ReleaseByteArrayElements(pps_, pps, 0);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_android_wllivepusher_push_WlPushVideo_pushVideoData(JNIEnv *env, jobject instance,
                                    jbyteArray data_, jint data_len, jboolean keyframe) {
    jbyte *data = env->GetByteArrayElements(data_, NULL);
    if (rtmpPush != NULL && !exit) {
        rtmpPush->pushVideoData(reinterpret_cast<char *>(data), data_len, keyframe);
    }
    env->ReleaseByteArrayElements(data_, data, 0);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_android_wllivepusher_push_WlPushVideo_pushAudioData(JNIEnv *env, jobject instance,
                          jbyteArray data_, jint data_len) {
    jbyte *data = env->GetByteArrayElements(data_, NULL);

    if (rtmpPush != NULL && !exit) {
        rtmpPush->pushAudioData(reinterpret_cast<char *>(data), data_len);
    }

    env->ReleaseByteArrayElements(data_, data, 0);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_android_wllivepusher_push_WlPushVideo_pushStop(JNIEnv *env, jobject instance) {
    if (rtmpPush != NULL) {
        exit = true;
        rtmpPush->pushStop();
        delete (rtmpPush);
        delete (wlCallJava);
        rtmpPush = NULL;
        wlCallJava = NULL;
    }
}