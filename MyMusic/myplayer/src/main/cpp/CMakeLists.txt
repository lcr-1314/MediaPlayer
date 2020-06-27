cmake_minimum_required(VERSION 3.4.1)

# 查找当前目录下的所有源文件，并保存到 DIR_LIB_SRCS 变量
aux_source_directory(${CMAKE_SOURCE_DIR} DIR_LIB_SRCS)

add_library( # Sets the name of the library.
        native-lib
        SHARED
        ${DIR_LIB_SRCS}
        WlFFmpeg.cpp
        )

find_library( # Sets the name of the path variable.
        log-lib
        log)

target_link_libraries( # Specifies the target library.
        native-lib
        ${log-lib})

find_library(android-lib android)

set(JNI_LIBS_DIR D:/MyMusic/myplayer/src/main/jniLibs/)

add_library(avutil-55
        SHARED
        IMPORTED )
set_target_properties(avutil-55
        PROPERTIES IMPORTED_LOCATION
        ${JNI_LIBS_DIR}/${ANDROID_ABI}/libavutil-55.so )

add_library(swresample-2
        SHARED
        IMPORTED )
set_target_properties(swresample-2
        PROPERTIES IMPORTED_LOCATION
        ${JNI_LIBS_DIR}/${ANDROID_ABI}/libswresample-2.so )

add_library(swscale-4
        SHARED
        IMPORTED )
set_target_properties(swscale-4
        PROPERTIES IMPORTED_LOCATION
        ${JNI_LIBS_DIR}/${ANDROID_ABI}/libswscale-4.so )

add_library(avcodec-57
        SHARED
        IMPORTED )
set_target_properties(avcodec-57
        PROPERTIES IMPORTED_LOCATION
        ${JNI_LIBS_DIR}/${ANDROID_ABI}/libavcodec-57.so )

add_library(avformat-57
        SHARED
        IMPORTED )
set_target_properties(avformat-57
        PROPERTIES IMPORTED_LOCATION
        ${JNI_LIBS_DIR}/${ANDROID_ABI}/libavformat-57.so )

add_library(avfilter-6
        SHARED
        IMPORTED )
set_target_properties(avfilter-6
        PROPERTIES IMPORTED_LOCATION
        ${JNI_LIBS_DIR}/${ANDROID_ABI}/libavfilter-6.so )

add_library(avdevice-57
        SHARED
        IMPORTED )
set_target_properties(avdevice-57
        PROPERTIES IMPORTED_LOCATION
        ${JNI_LIBS_DIR}/${ANDROID_ABI}/libavdevice-57.so )

include_directories(${JNI_LIBS_DIR}/include)

target_link_libraries(native-lib
        avutil-55
        swresample-2
        swscale-4
        avcodec-57
        avformat-57
        avfilter-6
        avdevice-57
        OpenSLES
        ${log-lib}
        ${android-lib} )