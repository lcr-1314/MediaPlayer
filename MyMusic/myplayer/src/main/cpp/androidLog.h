//
// Created by lenovo on 2020/4/23.
//

#ifndef MYMUSIC_ANDROIDLOG_H
#define MYMUSIC_ANDROIDLOG_H

#endif //MYMUSIC_ANDROIDLOG_H

#include "android/log.h"

#define LOG_DEBUG true

#define LOGI(FORMAT,...) __android_log_print(ANDROID_LOG_INFO,"lcr",FORMAT,##__VA_ARGS__);
#define LOGD(FORMAT,...) __android_log_print(ANDROID_LOG_DEBUG,"lcr",FORMAT,##__VA_ARGS__);
#define LOGW(FORMAT,...) __android_log_print(ANDROID_LOG_WARN,"lcr",FORMAT,##__VA_ARGS__);
#define LOGE(FORMAT,...) __android_log_print(ANDROID_LOG_ERROR,"lcr",FORMAT,##__VA_ARGS__);
#define LOGV(FORMAT,...) __android_log_print(ANDROID_LOG_VERBOSE,"lcr",FORMAT,##__VA_ARGS__);