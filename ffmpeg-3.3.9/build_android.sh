#bin/bash
#export NDK_HOME=/usr/work/ndk/android-ndk-r14b
export NDK_HOME=/home/liangcr/Desktop/android-ndk-r14b
#
#《Android音视频开发详解视频教程》https://edu.csdn.net/lecturer/1846
#
# Github：https://github.com/wanliyang1990/
# CSDN：https://blog.csdn.net/ywl5320/
#
# 包含：音视频播放器，摄像头预览，视频编码，音视频合成，直播推流等
# 涉及：ffmpeg、mediacodec、opengl、opensl、ndk、c++、camera、等核心技术知识
#
# By：ywl5320
#

function build
{
	echo "start build ffmpeg for $CPU"
	./configure --target-os=linux \
	--prefix=$PREFIX --arch=$CPU \
	--disable-doc \
	--enable-shared \
	--disable-static \
	--disable-yasm \
	--disable-asm \
	--disable-symver \
	--disable-openssl \
	--disable-encoders \
	--disable-programs \
 	--disable-htmlpages \
  	--disable-manpages \
  	--disable-podpages \
  	--disable-txtpages \
	--disable-muxers \
	--disable-ffmpeg \
	--disable-ffplay \
	--disable-ffprobe \
	--enable-avdevice \
	--enable-avfilter \
	--disable-debug \
	--cross-prefix=$CROSS_COMPILE \
	--enable-cross-compile \
	--sysroot=$SYSROOT \
	--enable-small \
	--enable-protocols \
	--extra-cflags="-Os -fpic $ADDI_CFLAGS $SSL_CFLAGS" \
	--extra-ldflags="$ADDI_LDFLAGS $SSL_LDFLAGS" \
	$ADDITIONAL_CONFIGURE_FLAG
	make clean
	make
	make install
	echo "build ffmpeg for $CPU finished"
}


#arm-v7a
PLATFORM_VERSION=android-14
SSL_CFLAGS=-I/usr/build/android/arm/include/
SSL_LDFLAGS=-L/usr/build/android/arm/lib/
ARCH=arm
CPU=armeabi-v7a
PREFIX=$(pwd)/android/$CPU
TOOLCHAIN=$NDK_HOME/toolchains/arm-linux-androideabi-4.9/prebuilt/linux-x86_64
CROSS_COMPILE=$TOOLCHAIN/bin/arm-linux-androideabi-
ADDI_CFLAGS="-march=armv7-a -mfloat-abi=softfp -mfpu=vfpv3-d16 -mthumb -mfpu=neon"
ADDI_LDFLAGS="-march=armv7-a -Wl,--fix-cortex-a8"
SYSROOT=$NDK_HOME/platforms/$PLATFORM_VERSION/arch-$ARCH/
#build


#arm64
PLATFORM_VERSION=android-21
SSL_CFLAGS=-I/usr/build/android/arm64/include/
SSL_LDFLAGS=-L/usr/build/android/arm64/lib/
ARCH=arm64
CPU=arm64
PREFIX=$(pwd)/android/$CPU
TOOLCHAIN=$NDK_HOME/toolchains/aarch64-linux-android-4.9/prebuilt/linux-x86_64
CROSS_COMPILE=$TOOLCHAIN/bin/aarch64-linux-android-
ADDI_CFLAGS=""
ADDI_LDFLAGS=""
SYSROOT=$NDK_HOME/platforms/$PLATFORM_VERSION/arch-$ARCH/
build


#x86
PLATFORM_VERSION=android-14
SSL_CFLAGS=-I/usr/build/android/x86/include/
SSL_LDFLAGS=-L/usr/build/android/x86/lib/
ARCH=x86
CPU=x86
PREFIX=$(pwd)/android/$CPU
TOOLCHAIN=$NDK_HOME/toolchains/x86-4.9/prebuilt/linux-x86_64
CROSS_COMPILE=$TOOLCHAIN/bin/i686-linux-android-
ADDI_CFLAGS="-march=i686 -mtune=intel -mssse3 -mfpmath=sse -m32"
ADDI_LDFLAGS=""
SYSROOT=$NDK_HOME/platforms/$PLATFORM_VERSION/arch-$ARCH/
#build

#x86_64
PLATFORM_VERSION=android-21
SSL_CFLAGS=-I/usr/build/android/x86_64/include/
SSL_LDFLAGS=-L/usr/build/android/x86_64/lib/
ARCH=x86_64
CPU=x86_64
PREFIX=$(pwd)/android/$CPU
TOOLCHAIN=$NDK_HOME/toolchains/x86_64-4.9/prebuilt/linux-x86_64
CROSS_COMPILE=$TOOLCHAIN/bin/x86_64-linux-android-
ADDI_CFLAGS="-march=x86-64 -msse4.2 -mpopcnt -m64 -mtune=intel"
ADDI_LDFLAGS=""
SYSROOT=$NDK_HOME/platforms/$PLATFORM_VERSION/arch-$ARCH/
#build





