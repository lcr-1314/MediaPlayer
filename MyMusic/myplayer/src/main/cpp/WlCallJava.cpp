//
// Created by lenovo on 2020/4/25.
//

#include "WlCallJava.h"

WlCallJava::WlCallJava(JavaVM *javaVM, JNIEnv *env, jobject *obj) {
    this->javaVM = javaVM;
    this->jniEnv = env;

    this->jobj = jniEnv->NewGlobalRef(*obj);    // 设置为全局变量，调用java函数的第一个参数
    jclass jlz = jniEnv->GetObjectClass(jobj);  // 获取对应方法ID的第一个参数

    if (!jlz) {
        if (LOG_DEBUG) {
            LOGE("get jclass wrong!");
        }
        return;
    }

    jmid_parpared = jniEnv->GetMethodID(jlz, "onCallParpared", "()V");
    jmid_load =  jniEnv->GetMethodID(jlz, "onCallLoad", "(Z)V");
    jmid_timeinfo = jniEnv->GetMethodID(jlz, "onCallTimeInfo","(II)V");
    jmid_error = jniEnv->GetMethodID(jlz, "onCallError", "(ILjava/lang/String;)V");
    jmid_complete = jniEnv->GetMethodID(jlz, "onCallComplete", "()V");
    jmid_renderyuv = jniEnv->GetMethodID(jlz, "onCallRenderYUV", "(II[B[B[B)V");
    jmid_supportvideo = jniEnv->GetMethodID(jlz, "onCallIsSupportMediaCodec", "(Ljava/lang/String;)Z");
    jmid_initmediacodec = jniEnv->GetMethodID(jlz, "initMediaCodec", "(Ljava/lang/String;II[B[B)V");
    jmid_decodeavpacket = jniEnv->GetMethodID(jlz, "decodeAVPacket","(I[B)V");
}

void WlCallJava::onCallParpared(int type) {
    if(type == MAIN_THREAD){
        jniEnv->CallVoidMethod(jobj, jmid_parpared);
    }else if(type == CHILD_THREAD){
        JNIEnv *jniEnv;
        if(javaVM->AttachCurrentThread(&jniEnv, 0) != JNI_OK){
            if(LOG_DEBUG){
                LOGE("get child thread jnienv wrong");
            }
            return;
        }
        jniEnv->CallVoidMethod(jobj, jmid_parpared);
        javaVM->DetachCurrentThread();
    }

}

void WlCallJava::onCallLoad(int type, bool load) {
    if (type == MAIN_THREAD) {
        jniEnv->CallVoidMethod(jobj, jmid_load, load);
    } else if (type == CHILD_THREAD) {
        JNIEnv *jniEnv;
        if (javaVM->AttachCurrentThread(&jniEnv, 0) != JNI_OK) {
            if (LOG_DEBUG) {
                LOGE("get child thread jnienv wrong");
            }
            return;
        }
        jniEnv->CallVoidMethod(jobj, jmid_load, load);
        javaVM->DetachCurrentThread();
    }
}

void WlCallJava::onCallTimeInfo(int type, int curr, int total) {
    if(type == MAIN_THREAD){
        jniEnv->CallVoidMethod(jobj, jmid_timeinfo, curr, total);
    }else if(type == CHILD_THREAD){
        JNIEnv *jniEnv;
        if(javaVM->AttachCurrentThread(&jniEnv, 0) != JNI_OK){
            if(LOG_DEBUG){
                LOGE("get child thread jnienv wrong");
            }
            return;
        }
        jniEnv->CallVoidMethod(jobj, jmid_timeinfo, curr, total);
        javaVM->DetachCurrentThread();
    }
}

WlCallJava::~WlCallJava() {

}

void WlCallJava::onCallError(int type, int code, char *msg) {
    if(type == MAIN_THREAD){
        jstring jmsg = jniEnv->NewStringUTF(msg);
        jniEnv->CallVoidMethod(jobj, jmid_error, code, jmsg);
        jniEnv->DeleteLocalRef(jmsg);
    }else if(type == CHILD_THREAD){
        JNIEnv *jniEnv;
        if(javaVM->AttachCurrentThread(&jniEnv, 0) != JNI_OK){
            if(LOG_DEBUG){
                LOGE("get child thread jnienv wrong");
            }
            return;
        }
        jstring jmsg = jniEnv->NewStringUTF(msg);
        jniEnv->CallVoidMethod(jobj, jmid_error, code, jmsg);
        jniEnv->DeleteLocalRef(jmsg);
        javaVM->DetachCurrentThread();
    }
}

void WlCallJava::onComplete(int type) {
    if(type == MAIN_THREAD){
        jniEnv->CallVoidMethod(jobj, jmid_complete);
    }else if(type == CHILD_THREAD){
        JNIEnv *jniEnv;
        if(javaVM->AttachCurrentThread(&jniEnv, 0) != JNI_OK){
            if(LOG_DEBUG){
                LOGE("get child thread jnienv wrong");
            }
            return;
        }
        jniEnv->CallVoidMethod(jobj, jmid_complete);
        javaVM->DetachCurrentThread();
    }
}

void WlCallJava::onCallRenderYUV(int width, int height, uint8_t *fy, uint8_t *fu, uint8_t *fv) {
    JNIEnv *jniEnv;
    if (javaVM->AttachCurrentThread(&jniEnv, 0) != JNI_OK) {
        if (LOG_DEBUG) {
            LOGE("get child thread jnienv wrong");
        }
        return;
    }

    jbyteArray y = jniEnv->NewByteArray(width * height);
    jniEnv->SetByteArrayRegion(y, 0, width * height, reinterpret_cast<const jbyte *>(fy));

    jbyteArray u = jniEnv->NewByteArray(width * height / 4);
    jniEnv->SetByteArrayRegion(u, 0, width * height / 4, reinterpret_cast<const jbyte *>(fu));

    jbyteArray v = jniEnv->NewByteArray(width * height / 4);
    jniEnv->SetByteArrayRegion(v, 0, width * height / 4, reinterpret_cast<const jbyte *>(fv));

    jniEnv->CallVoidMethod(jobj, jmid_renderyuv, width, height, y, u, v);

    jniEnv->DeleteLocalRef(y);
    jniEnv->DeleteLocalRef(u);
    jniEnv->DeleteLocalRef(v);

    javaVM->DetachCurrentThread();
}

bool WlCallJava::onCallIsSupportVideo(const char *ffcodecname) {

    bool support = false;
    JNIEnv *jniEnv;
    if (javaVM->AttachCurrentThread(&jniEnv, 0) != JNI_OK) {
        if (LOG_DEBUG) {
            LOGE("get child thread jnienv wrong");
        }
        return support;
    }

    jstring type = jniEnv->NewStringUTF(ffcodecname);
    support = jniEnv->CallBooleanMethod(jobj, jmid_supportvideo, type);
    jniEnv->DeleteLocalRef(type);
    javaVM->DetachCurrentThread();

    return support;
}

void WlCallJava::onCallInitMediacodec(const char* mime, int width, int height, int csd0_size, int csd1_size, uint8_t *csd_0, uint8_t *csd_1) {
    JNIEnv *jniEnv;
    if (javaVM->AttachCurrentThread(&jniEnv, 0) != JNI_OK) {
        if (LOG_DEBUG) {
            LOGE("call onCallComplete worng");
        }
    }

    jstring type = jniEnv->NewStringUTF(mime);
    jbyteArray csd0 = jniEnv->NewByteArray(csd0_size);
    jniEnv->SetByteArrayRegion(csd0, 0, csd0_size, reinterpret_cast<const jbyte *>(csd_0));
    jbyteArray csd1 = jniEnv->NewByteArray(csd1_size);
    jniEnv->SetByteArrayRegion(csd1, 0, csd1_size, reinterpret_cast<const jbyte *>(csd_1));

    jniEnv->CallVoidMethod(jobj, jmid_initmediacodec, type, width, height, csd0, csd1);

    jniEnv->DeleteLocalRef(csd0);
    jniEnv->DeleteLocalRef(csd1);
    jniEnv->DeleteLocalRef(type);
    javaVM->DetachCurrentThread();
}

void WlCallJava::onCallDecodeAVPacket(int datasize, uint8_t *packetdata) {
    JNIEnv *jniEnv;
    if (javaVM->AttachCurrentThread(&jniEnv, 0) != JNI_OK) {
        if (LOG_DEBUG) {
            LOGE("call onCallComplete worng");
        }
    }
    jbyteArray data = jniEnv->NewByteArray(datasize);
    jniEnv->SetByteArrayRegion(data, 0, datasize, reinterpret_cast<const jbyte *>(packetdata));

    jniEnv->CallVoidMethod(jobj, jmid_decodeavpacket, datasize, data);
    jniEnv->DeleteLocalRef(data);

    javaVM->DetachCurrentThread();
}
