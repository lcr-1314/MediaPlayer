#!/bin/bash
export NDK=D:/NDK/android-ndk-r14b

function build_one
{
./configure \
--prefix=$PREFIX \
--disable-shared \
--enable-static \
--enable-asm \
--enable-neon \
--disable-doc \
--disable-ffmpeg \
--disable-ffplay \
--disable-ffprobe \
--disable-ffserver \
--disable-doc \
--disable-symver \
--enable-pthreads \
--enable-small \
--enable-jni \
--enable-mediacodec \
--enable-runtime-cpudetect \
--cross-prefix=$CROSS_COMPILE \
--target-os=android \
--arch=arm \
--enable-cross-compile \
--sysroot=$SYSROOT \
--extra-cflags="$ADDI_CFLAGS" \
--extra-ldflags="$ADDI_LDFLAGS" \
$ADDITIONAL_CONFIGURE_FLAG
make clean
make
make install

echo "COMPILE finish ffmpeg!"
#打包
$TOOLCHAIN/bin/arm-linux-androideabi-ld \
    -rpath-link=$SYSROOT/usr/lib \
    -L$SYSROOT/usr/lib \
    -L$PREFIX/lib \
    -soname libffmpeg.so -shared -nostdlib -Bsymbolic --whole-archive --no-undefined -o \
    $PREFIX/libffmpeg.so \
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
}

#armeabi-v7a
PLATFORM_VERSION=android-14
ARCH=arm
CPU=armeabi-v7a
SYSROOT=$NDK/platforms/$PLATFORM_VERSION/arch-$ARCH
TOOLCHAIN=$NDK/toolchains/arm-linux-androideabi-4.9/prebuilt/windows-x86_64
PREFIX=D:/Forffmpeg/ffmpeg-3.3.9/ffmpeglibs/$CPU
CROSS_COMPILE=$TOOLCHAIN/bin/arm-linux-androideabi-
ADDI_CFLAGS="-march=armv7-a -mfloat-abi=softfp -mfpu=vfpv3-d16 -mthumb -mfpu=neon"
ADDI_LDFLAGS="-march=armv7-a -Wl,--fix-cortex-a8"
build_one

#arm
#PLATFORM_VERSION=android-14
#ARCH=arm
#CPU=arm
#SYSROOT=$NDK/platforms/$PLATFORM_VERSION/arch-$ARCH
#TOOLCHAIN=$NDK/toolchains/arm-linux-androideabi-4.9/prebuilt/windows-x86_64
#PREFIX=D:/Forffmpeg/ffmpeg-3.3.9/ffmpeglibs/$CPU
#CROSS_COMPILE=$TOOLCHAIN/bin/arm-linux-androideabi-
#ADDI_CFLAGS="-marm"
#build_one

#arm64
#PLATFORM_VERSION=android-21
#ARCH=arm64
#CPU=arm64
#SYSROOT=$NDK/platforms/$PLATFORM_VERSION/arch-$ARCH
#PREFIX=D:/Forffmpeg/ffmpeg-3.3.9/ffmpeglibs/$CPU
#TOOLCHAIN=$NDK/toolchains/aarch64-linux-android-4.9/prebuilt/windows-x86_64
#CROSS_COMPILE=$TOOLCHAIN/bin/aarch64-linux-android-
#ADDI_CFLAGS="-march -mthumb"
#ADDI_LDFLAGS=""
#build_one

#x86
#PLATFORM_VERSION=android-14
#ARCH=x86
#CPU=x86
#SYSROOT=$NDK/platforms/$PLATFORM_VERSION/arch-$ARCH/
#PREFIX=D:/Forffmpeg/ffmpeg-3.3.9/ffmpeglibs/$CPU
#TOOLCHAIN=$NDK/toolchains/x86-4.9/prebuilt/windons-x86_64
#CROSS_COMPILE=$TOOLCHAIN/bin/i686-linux-android-
#ADDI_CFLAGS="-march=i686 -mtune=intel -mssse3 -mfpmath=sse -m32"
#ADDI_LDFLAGS=""
#build_one

#x86_64
#PLATFORM_VERSION=android-21
#ARCH=x86_64
#CPU=x86_64
#SYSROOT=$NDK/platforms/$PLATFORM_VERSION/arch-$ARCH
#PREFIX=D:/Forffmpeg/ffmpeg-3.3.9/ffmpeglibs/$CPU
#TOOLCHAIN=$NDK/toolchains/x86_64-4.9/prebuilt/windows-x86_64
#CROSS_COMPILE=$TOOLCHAIN/bin/x86_64-linux-android-
#ADDI_CFLAGS="-march=x86-64 -msse4.2 -mpopcnt -m64 -mtune=intel"
#ADDI_LDFLAGS=""
#build_one


read -p "Press any key to continue."