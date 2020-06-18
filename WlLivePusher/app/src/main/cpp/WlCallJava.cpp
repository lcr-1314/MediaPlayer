#include "WlCallJava.h"

WlCallJava::WlCallJava(JavaVM *javaVM, JNIEnv *jniEnv, jobject *jobj) {
    this->javaVM = javaVM;
    this->jniEnv = jniEnv;
    this->jobj = jniEnv->NewGlobalRef(*jobj);

    jclass jlz = jniEnv->GetObjectClass(this->jobj);

    jmid_connecting =jniEnv->GetMethodID(jlz, "onConnecting", "()V");
    jmid_connectsuccess = jniEnv->GetMethodID(jlz, "onConnectSuccess", "()V");
    jmid_connectfail = jniEnv->GetMethodID(jlz, "onConnectFial", "(Ljava/lang/String;)V");
    jmid_audiodata = jniEnv->GetMethodID(jlz, "onAudioPcmData", "([BI)V");
}

WlCallJava::~WlCallJava() {
    jniEnv->DeleteGlobalRef(jobj);
    javaVM = NULL;
    jniEnv = NULL;
}

void WlCallJava::onConnectint(int type) {

    if(type == WL_THREAD_CHILD)
    {
        JNIEnv *jniEnv;
        if(javaVM->AttachCurrentThread(&jniEnv, 0) != JNI_OK)
        {
            return;
        }
        jniEnv->CallVoidMethod(jobj, jmid_connecting);
        javaVM->DetachCurrentThread();
    }
    else
    {
        jniEnv->CallVoidMethod(jobj, jmid_connecting);
    }
}

void WlCallJava::onConnectsuccess() {
    JNIEnv *jniEnv;
    if(javaVM->AttachCurrentThread(&jniEnv, 0) != JNI_OK)
    {
        return;
    }
    jniEnv->CallVoidMethod(jobj, jmid_connectsuccess);
    javaVM->DetachCurrentThread();
}

void WlCallJava::onConnectFail(char *msg) {

    JNIEnv *jniEnv;
    if(javaVM->AttachCurrentThread(&jniEnv, 0) != JNI_OK)
    {
        return;
    }

    jstring jmsg = jniEnv->NewStringUTF(msg);

    jniEnv->CallVoidMethod(jobj, jmid_connectfail, jmsg);

    jniEnv->DeleteLocalRef(jmsg);
    javaVM->DetachCurrentThread();
}

void WlCallJava::onAudioData(int type, void *buffer, int size) {
    if (type == WL_THREAD_CHILD) {
        JNIEnv *jniEnv;
        if (javaVM->AttachCurrentThread(&jniEnv, 0) != JNI_OK) {
            return;
        }
        jbyteArray jbuffer = jniEnv->NewByteArray(size);
        jniEnv->SetByteArrayRegion(jbuffer, 0, size, static_cast<const jbyte *>(buffer));
        jniEnv->CallVoidMethod(jobj, jmid_audiodata, jbuffer, size);
        jniEnv->DeleteLocalRef(jbuffer);
        javaVM->DetachCurrentThread();
    } else {
        jbyteArray jbuffer = jniEnv->NewByteArray(size);
        jniEnv->SetByteArrayRegion(jbuffer, 0, size, static_cast<const jbyte *>(buffer));
        jniEnv->CallVoidMethod(jobj, jmid_audiodata, jbuffer, size);
        jniEnv->DeleteLocalRef(jbuffer);
    }
}
