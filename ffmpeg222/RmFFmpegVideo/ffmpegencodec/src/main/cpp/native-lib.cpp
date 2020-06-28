#include <jni.h>
#include <string>
#include <malloc.h>

#include "jx_log.h"
#include "ffmpeg.h"
#include "jx_user_arguments.h"
#include "base_include.h"

extern "C"{
#include "libavcodec/avcodec.h"
#include "libavformat/avformat.h"
}

char *logUrl;

//JXYUVEncodeH264 *h264_encoder;
//JXPCMEncodeAAC *aac_encoder;

#define VIDEO_FORMAT ".h264"
#define MEDIA_FORMAT ".mp4"
#define AUDIO_FORMAT ".aac"

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

//void log_callback(void *ptr, int level, const char *fmt, va_list vl) {
//    FILE *fp = NULL;
//
//    if (!fp)
//        fp = fopen(logUrl, "a+");
//
//    if (fp) {
//        vfprintf(fp, fmt, vl);
//        fflush(fp);
//        fclose(fp);
//    }
//}

//extern "C"
//JNIEXPORT void JNICALL
//Java_com_rayman_ffmpegencodec_jniinterface_FFmpegBridge_initJXFFmpeg(JNIEnv *env, jclass clazz,
//                                                                     jboolean debug,
//                                                                     jstring log_Url_) {
//    JNI_DEBUG = debug;
//    if(JNI_DEBUG && log_Url_ != NULL){
//        av_log_set_callback(log_callback);
//    }
//
//    const char* logMsg = env->GetStringUTFChars(log_Url_, 0);
//    if (logMsg != NULL) {
//        LOGE(JNI_DEBUG, "============strlen:%d", strlen(logMsg));
//        logUrl = (char *) (malloc(strlen(logMsg)));
//    }
//    env->ReleaseStringUTFChars(log_Url_, logMsg);
//}
//
//int ffmpeg_cmd_run(int argc, char **argv){
//    return jxRun(argc, argv);
//}

//extern "C"
//JNIEXPORT jint JNICALL
//Java_com_rayman_ffmpegencodec_jniinterface_FFmpegBridge_jxCMDRun(JNIEnv *env, jclass clazz,
//                                                                 jobjectArray commands) {
//    int argc = env->GetArrayLength(commands);
//    char *argv[argc];
//    int i;
//    for (i = 0; i < argc; i++) {
//        jstring js = (jstring) env->GetObjectArrayElement(commands, i);
//        argv[i] = (char *) env->GetStringUTFChars(js, 0);
//    }
//
//    return ffmpeg_cmd_run(argc,argv);
//}

jstring getEncoderConfigInfo(JNIEnv *env) {
    char info[10000] = {0};
    sprintf(info, "%s\n", avcodec_configuration());
    return env->NewStringUTF(info);
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_rayman_ffmpegencodec_jniinterface_FFmpegBridge_getFFmpegConfig(JNIEnv *env, jclass clazz) {
    // TODO: implement getFFmpegConfig()
    return getEncoderConfigInfo(env);
}

//extern "C"
//JNIEXPORT jint JNICALL
//Java_com_rayman_ffmpegencodec_jniinterface_FFmpegBridge_encodeFrame2H264(JNIEnv *env, jclass clazz,
//                                                                         jbyteArray data_) {
//    // TODO: implement encodeFrame2H264()
//    jbyte *elements = env->GetByteArrayElements(data_, 0);
//    int i = h264_encoder->startSendOneFrame((uint8_t *) elements);
//    env->ReleaseByteArrayElements(data_,elements,0);
//    return i;
//}

//extern "C"
//JNIEXPORT jint JNICALL
//Java_com_rayman_ffmpegencodec_jniinterface_FFmpegBridge_prepareJXFFmpegEncoder(JNIEnv *env,
//                                                                               jclass type,
//                                                                               jstring media_base_path_,
//                                                                               jstring media_name_,
//                                                                               jint v_custom_format,
//                                                                               jint in_width,
//                                                                               jint in_height,
//                                                                               jint out_width,
//                                                                               jint out_height,
//                                                                               jint frame_rate,
//                                                                               jlong video_bit_rate) {
//    // TODO: implement prepareJXFFmpegEncoder()
//    jclass global_class = (jclass) env->NewGlobalRef(type);
//    UserArguments *arguments = (UserArguments *) malloc(sizeof(UserArguments));
//    const char *media_base_path = env->GetStringUTFChars(media_base_path_, 0);
//    const char *media_name = env->GetStringUTFChars(media_name_, 0);
//    JXJNIHandler *jni_handler = new JXJNIHandler();
//    jni_handler->setup_audio_state(START_STATE);//setup_audio_state(START_STATE);
//    jni_handler->setup_video_state(START_STATE);
//    arguments->media_base_path = media_base_path;
//    arguments->media_name = media_name;
//
//    size_t v_path_size = strlen(media_base_path) + strlen(media_name) + strlen(VIDEO_FORMAT) + 1;
//    arguments->video_path = (char *) malloc(v_path_size + 1);
//
//    size_t a_path_size = strlen(media_base_path) + strlen(media_name) + strlen(AUDIO_FORMAT) + 1;
//    arguments->audio_path = (char *) malloc(a_path_size + 1);
//
//    size_t m_path_size = strlen(media_base_path) + strlen(media_name) + strlen(MEDIA_FORMAT) + 1;
//    arguments->media_path = (char *) malloc(m_path_size + 1);
//
//    strcpy(arguments->video_path, media_base_path);
//    strcat(arguments->video_path, "/");
//    strcat(arguments->video_path, media_name);
//    strcat(arguments->video_path, VIDEO_FORMAT);
//
//    strcpy(arguments->audio_path, media_base_path);
//    strcat(arguments->audio_path, "/");
//    strcat(arguments->audio_path, media_name);
//    strcat(arguments->audio_path, AUDIO_FORMAT);
//
//    strcpy(arguments->media_path, media_base_path);
//    strcat(arguments->media_path, "/");
//    strcat(arguments->media_path, media_name);
//    strcat(arguments->media_path, MEDIA_FORMAT);
//
//    arguments->video_bit_rate = video_bit_rate;
//    arguments->frame_rate = frame_rate;
//    arguments->audio_bit_rate = 40000;
//    arguments->audio_sample_rate = 44100;
//    arguments->in_width = in_width;
//    arguments->in_height = in_height;
//    arguments->out_height = out_height;
//    arguments->out_width = out_width;
//    arguments->v_custom_format = v_custom_format;
//    arguments->handler = jni_handler;
//    arguments->env = env;
//    arguments->java_class = global_class;
//    arguments->env->GetJavaVM(&arguments->javaVM);
//    h264_encoder = new JXYUVEncodeH264(arguments);
//    aac_encoder = new JXPCMEncodeAAC(arguments);
//    int v_code = h264_encoder->initVideoEncoder();
//    int a_code = aac_encoder->initAudioEncoder();
//
//    if (v_code == 0 && a_code == 0) {
//        return 0;
//    } else {
//        return -1;
//    }
//}