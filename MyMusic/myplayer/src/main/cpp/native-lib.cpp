#include <jni.h>
#include <string>
#include "androidLog.h"

#include "WlCallJava.h"
#include "WlFFmpeg.h"

JavaVM *javaVM = NULL;
WlCallJava *callJava = NULL;
WlFFmpeg *fFmpeg = NULL;
WlPlaystatus *playstatus = NULL;

bool nexit = true;
pthread_t thread_start;


extern "C"
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    jint result = -1;
    javaVM = vm;

    JNIEnv *env;
    if (vm->GetEnv((void **) (&env), JNI_VERSION_1_4) != JNI_OK) {
        return result;
    }

    return JNI_VERSION_1_4;
}

// 准备
extern "C"
JNIEXPORT void JNICALL
Java_com_android_myplayer_player_WlPlayer_n_1parpared(JNIEnv *env,
                                                      jobject obj,
                                                      jstring source_) {
    const char *source = env->GetStringUTFChars(source_, 0);

    if (fFmpeg == NULL) {
        if (callJava == NULL) {
            callJava = new WlCallJava(javaVM, env, &obj);  // 初始化调用java的类
        }
        callJava->onCallLoad(MAIN_THREAD, true);// 调用java函数，通知java层，正在加载中 ...

        playstatus = new WlPlaystatus();        // 初始化播放状态类

        fFmpeg = new WlFFmpeg(playstatus, callJava, source);    // 初始化ffmpeg解码类
        fFmpeg->parpared();
    }
}

void *startCallBack(void *data) {
    WlFFmpeg *fFmpeg = (WlFFmpeg *) data;
    fFmpeg->start();                // 进入播放
    pthread_exit(&thread_start);    // 退出播放线程
}

extern "C"
JNIEXPORT void JNICALL
Java_com_android_myplayer_player_WlPlayer_n_1start(JNIEnv *env, jobject thiz) {
    if (fFmpeg != NULL) {
        pthread_create(&thread_start, NULL, startCallBack, fFmpeg); // 创建播放线程
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_android_myplayer_player_WlPlayer_n_1pause(JNIEnv *env, jobject thiz) {
    if(fFmpeg != NULL){
        fFmpeg->pause();
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_android_myplayer_player_WlPlayer_n_1resume(JNIEnv *env, jobject thiz) {
    if (fFmpeg != NULL) {
        fFmpeg->resume();
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_android_myplayer_player_WlPlayer_n_1stop(JNIEnv *env, jobject thiz) {
    if (!nexit) {
        return;
    }

    jclass jlz = env->GetObjectClass(thiz);
    jmethodID jmid_next = env->GetMethodID(jlz, "onCallNext", "()V");

    nexit = false;

    if (fFmpeg != NULL) {
        fFmpeg->release();
        delete (fFmpeg);
        fFmpeg = NULL;

        if (callJava != NULL) {
            delete (callJava);
            callJava = NULL;
        }

        if (playstatus != NULL) {
            delete (playstatus);
            playstatus = NULL;
        }
    }
    nexit = true;

    env->CallVoidMethod(thiz, jmid_next);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_android_myplayer_player_WlPlayer_n_1seek(JNIEnv *env, jobject thiz, jint seek) {
    if (fFmpeg != NULL) {
        fFmpeg->seek(seek);
    }
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_android_myplayer_player_WlPlayer_n_1duration(JNIEnv *env, jobject thiz) {
    if (fFmpeg != NULL) {
        return fFmpeg->duration;
    }
    return 0;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_android_myplayer_player_WlPlayer_n_1volume(JNIEnv *env, jobject thiz, jint percent) {
    if (fFmpeg != NULL) {
        fFmpeg->setVolume(percent);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_android_myplayer_player_WlPlayer_n_1mute(JNIEnv *env, jobject thiz, jint mute) {
    if (fFmpeg != NULL) {
        fFmpeg->setMute(mute);
    }
}