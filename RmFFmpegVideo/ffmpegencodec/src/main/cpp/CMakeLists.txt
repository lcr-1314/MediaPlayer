cmake_minimum_required(VERSION 3.4.1)

add_library( # Sets the name of the library.
             native-lib
             SHARED
                jx_log.cpp
             native-lib.cpp )

#D:\ThridProject\RmFFmpegVideo\ffmpegencodec\src
find_library( # Sets the name of the path variable.
              log-lib
              log )

set(JNI_LIBS_DIR D:/ThridProject/RmFFmpegVideo/ffmpegencodec/src/main/jniLibs/)

#D:/ThridProject/RmFFmpegVideo/ffmpegencodec/src/main/jniLibs/
add_library(
        avcodec
        SHARED
        IMPORTED
)

set_target_properties(
        avcodec
        PROPERTIES IMPORTED_LOCATION
        ${JNI_LIBS_DIR}/${ANDROID_ABI}/libavcodec.so
)

add_library(
        avfilter
        SHARED
        IMPORTED
)
set_target_properties(
        avfilter
        PROPERTIES IMPORTED_LOCATION
        ${JNI_LIBS_DIR}/${ANDROID_ABI}/libavfilter.so
)

add_library(
        avformat
        SHARED
        IMPORTED
)
set_target_properties(
        avformat
        PROPERTIES IMPORTED_LOCATION
        ${JNI_LIBS_DIR}/${ANDROID_ABI}/libavformat.so
)

add_library(
        avutil
        SHARED
        IMPORTED
)
set_target_properties(
        avutil
        PROPERTIES IMPORTED_LOCATION
        ${JNI_LIBS_DIR}/${ANDROID_ABI}/libavutil.so
)

add_library(
        swresample
        SHARED
        IMPORTED
)
set_target_properties(
        swresample
        PROPERTIES IMPORTED_LOCATION
        ${JNI_LIBS_DIR}/${ANDROID_ABI}/libswresample.so
)

add_library(
        swscale
        SHARED
        IMPORTED
)
set_target_properties(
        swscale
        PROPERTIES IMPORTED_LOCATION
        ${JNI_LIBS_DIR}/${ANDROID_ABI}/libswscale.so
)

add_library(
        fdk-aac
        SHARED
        IMPORTED
)
set_target_properties(
        fdk-aac
        PROPERTIES IMPORTED_LOCATION
        ${JNI_LIBS_DIR}/${ANDROID_ABI}/libfdk-aac.so
)

include_directories(${JNI_LIBS_DIR}/include)

target_link_libraries( # Specifies the target library.
                       native-lib android
        avcodec avfilter avformat avutil swresample swscale fdk-aac
                       ${log-lib} )