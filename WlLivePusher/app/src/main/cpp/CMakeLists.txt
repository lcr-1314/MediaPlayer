
cmake_minimum_required(VERSION 3.4.1)

set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -DNO_CRYPTO")

add_library( # Sets the name of the library.
             wlpush

             SHARED
             MyAudio.cpp
             RecordBuffer.cpp

             wlpush.cpp
             WlQueue.cpp
             RtmpPush.cpp
             WlCallJava.cpp

             librtmp/amf.c
             librtmp/hashswf.c
             librtmp/log.c
             librtmp/parseurl.c
             librtmp/rtmp.c
             )

find_library( # Sets the name of the path variable.
              log-lib
              log )

target_link_libraries( # Specifies the target library.
                       wlpush
                       android
                       OpenSLES
                       ${log-lib} )