#include <jni.h>
#include <string>
#include <malloc.h>

#include "jx_log.h"

extern "C"{
#include "libavcodec/avcodec.h"
#include "libavformat/avformat.h"
}

char *logUrl;

//extern "C"
//JNIEXPORT jstring JNICALL
//Java_com_rayman_ffmpegencodec_FFmpegEncodec_stringFromJNI(JNIEnv *env,
//        jobject thiz) {
//    std::string hello = "Hello from C++";
//
//    avcodec_register_all();
//    avformat_network_init();
//
//
//    return env->NewStringUTF(hello.c_str());
//}

void log_callback(void *ptr, int level, const char *fmt, va_list vl) {
    FILE *fp = NULL;

    if (!fp)
        fp = fopen(logUrl, "a+");

    if (fp) {
        vfprintf(fp, fmt, vl);
        fflush(fp);
        fclose(fp);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_rayman_ffmpegencodec_jniinterface_FFmpegBridge_initJXFFmpeg(JNIEnv *env, jclass clazz,
                                                                     jboolean debug,
                                                                     jstring log_Url_) {
    JNI_DEBUG = debug;
    if(JNI_DEBUG && log_Url_ != NULL){
        av_log_set_callback(log_callback);
    }

    const char* logMsg = env->GetStringUTFChars(log_Url_, 0);
    if (logMsg != NULL) {
        LOGE(JNI_DEBUG, "============strlen:%d", strlen(logMsg));
        logUrl = (char *) (malloc(strlen(logMsg)));
    }
    env->ReleaseStringUTFChars(log_Url_, logMsg);
}

