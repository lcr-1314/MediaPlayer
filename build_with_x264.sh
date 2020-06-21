#!/bin/bash
NDK=/home/liangcr/Desktop/android-ndk-r14b
SYSROOT=$NDK/platforms/android-14/arch-arm
TOOLCHAIN=$NDK/toolchains/arm-linux-androideabi-4.9/prebuilt/linux-x86_64

CPU=armeabi-v7a
ARCH=arm
PREFIX=$(pwd)/android/$CPU

#ADDI_CFLAGS="-marm"
#ADDI_CFLAGS="-march=armv7-a -mfloat-abi=softfp -mfpu=vfpv3-d16 -mthumb -mfpu=neon"
#ADDI_LDFLAGS="-march=armv7-a -Wl,--fix-cortex-a8"
ADDI_CFLAGS="-I./libx264/include"
ADDI_LDFLAGS="-L./libx264/lib"

#配置
./configure \
    --prefix=$PREFIX \
    --arch=$ARCH \
    --cross-prefix=$TOOLCHAIN/bin/arm-linux-androideabi- \
    --extra-ldflags="$ADDI_LDFLAGS" \
    --sysroot=$SYSROOT \
    --extra-cflags="-Os -fpic $ADDI_CFLAGS" \
    --target-os=linux \
    --enable-cross-compile \
    --enable-gpl \
    --disable-shared \
    --enable-static \
    --enable-libx264 \
    --enable-encoder=libx264 \
    --disable-doc \
    --disable-debug \
    --enable-small \
    --disable-programs \
    --disable-ffmpeg \
    --disable-ffplay \
    --disable-ffprobe \
    --disable-ffserver \
    $ADDITIONAL_CONFIGURE_FLAG

#编译
make clean

make -j4

make install

#打包
$TOOLCHAIN/bin/arm-linux-androideabi-ld \
    -rpath-link=$SYSROOT/usr/lib \
    -L$SYSROOT/usr/lib \
    -L$PREFIX/lib \
    -soname libffmpeg.so -shared -nostdlib -Bsymbolic --whole-archive --no-undefined -o \
    $PREFIX/libffmpeg.so \
    libx264/lib/libx264.a\
    libavcodec/libavcodec.a \
    libavfilter/libavfilter.a \
    libswresample/libswresample.a \
    libavformat/libavformat.a \
    libavutil/libavutil.a \
    libswscale/libswscale.a \
    libavdevice/libavdevice.a \
    libpostproc/libpostproc.a \
    -lc -lm -lz -ldl -llog --dynamic-linker=/system/bin/linker \
    $TOOLCHAIN/lib/gcc/arm-linux-androideabi/4.9.x/libgcc.a

#strip
$TOOLCHAIN/bin/arm-linux-androideabi-strip  $PREFIX/libffmpeg.so
