package com.android.wllivepusher.push;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;

import com.android.wllivepusher.egl.EglHelper;
import com.android.wllivepusher.egl.WLEGLSurfaceView;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;

import javax.microedition.khronos.egl.EGLContext;

//抽象类
public abstract class WlBasePushEncoder {
    private Surface surface;
    private EGLContext eglContext;

    private int width;
    private int height;

    private MediaCodec videoEncodec;
    private MediaFormat videoFormat;
    private MediaCodec.BufferInfo videoBufferinfo;

    private MediaCodec audioEncodec;
    private MediaFormat audioFormat;
    private MediaCodec.BufferInfo audioBufferinfo;
    private long audioPts = 0;
    private int sampleRate;

    private WlEGLMediaThread wlEGLMediaThread;
    private VideoEncodecThread videoEncodecThread;
    private AudioEncodecThread audioEncodecThread;
    private WlAudioRecordUitl wlAudioRecordUitl;

    private WLEGLSurfaceView.WlGLRender wlGLRender; // 渲染

    public final static int RENDERMODE_WHEN_DIRTY = 0;
    public final static int RENDERMODE_CONTINUOUSLY = 1;
    private int mRenderMode = RENDERMODE_CONTINUOUSLY;

    private OnMediaInfoListener onMediaInfoListener;

    public WlBasePushEncoder(Context context) {
    }

    public void setRender(WLEGLSurfaceView.WlGLRender wlGLRender) {
        this.wlGLRender = wlGLRender;
    }

    public void setmRenderMode(int mRenderMode) {
        if (wlGLRender == null) {
            throw new RuntimeException("must set render before");
        }

        this.mRenderMode = mRenderMode;
    }

    public void setOnMediaInfoListener(OnMediaInfoListener onMediaInfoListener) {
        this.onMediaInfoListener = onMediaInfoListener;
    }

    public void initEncodec(EGLContext eglContext, int width, int height) {
        this.width = width;
        this.height = height;
        this.eglContext = eglContext; // Egl上下文为外部传入

        initMediaEncodec(width, height, 44100, 2);
    }

    public void startRecord() {
        if (surface != null && eglContext != null) {
            audioPts = 0;

            wlEGLMediaThread = new WlEGLMediaThread(new WeakReference<WlBasePushEncoder>(this));
            videoEncodecThread = new VideoEncodecThread(new WeakReference<WlBasePushEncoder>(this));
            audioEncodecThread = new AudioEncodecThread(new WeakReference<WlBasePushEncoder>(this));
            wlEGLMediaThread.isCreate = true;
            wlEGLMediaThread.isChange = true;

            wlEGLMediaThread.start();           // run()
            videoEncodecThread.start();

            audioEncodecThread.start();
            wlAudioRecordUitl.startRecord("", false, false);    // 开始录音
        }
    }

    public void stopRecord() {
        if (wlEGLMediaThread != null && videoEncodecThread != null && audioEncodecThread != null) {
            wlAudioRecordUitl.stopRecord();
            videoEncodecThread.exit();
            audioEncodecThread.exit();
            wlEGLMediaThread.onDestory();
            videoEncodecThread = null;
            wlEGLMediaThread = null;
            audioEncodecThread = null;
        }
    }

    // 初始化解码器
    private void initMediaEncodec(int width, int height, int sampleRate, int channelCount) {
        initVideoEncodec(MediaFormat.MIMETYPE_VIDEO_AVC, width, height);
        initAudioEncodec(MediaFormat.MIMETYPE_AUDIO_AAC, sampleRate, channelCount);
        initPCMRecord();
    }

    private void initPCMRecord() {
        wlAudioRecordUitl = new WlAudioRecordUitl();
        wlAudioRecordUitl.setOnRecordLisener(new WlAudioRecordUitl.OnRecordLisener() {
            @Override
            public void recordByte(byte[] audioData, int readSize) {
                if (wlAudioRecordUitl.isStart()) {
                    putPCMData(audioData, readSize);
                }
            }
        });
    }

    private void initVideoEncodec(String mimeType, int width, int height) {
        try {
            videoBufferinfo = new MediaCodec.BufferInfo();
            //MediaCodec编码Video，
            // 必须设置KEY_MIME、KEY_BIT_RATE、KEY_WIDTH、KEY_HEIGHT
            // 、KEY_COLOR_FORMAT、KYE_FRAME_RATE及KEY_I_FRAME_INTERVAL这七个.
            videoFormat = MediaFormat.createVideoFormat(mimeType, width, height);
            videoFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
            videoFormat.setInteger(MediaFormat.KEY_BIT_RATE, width * height * 4);
            videoFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 30); // 帧率.
            videoFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);//关键帧间隔时间 单位s

            videoEncodec = MediaCodec.createEncoderByType(mimeType);
            videoEncodec.configure(videoFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);

            // 编码视频使用Surface模式 //其它模式还有 YUV缓存 及其它特定的格式
            // 对于 API 19 以上的系统，可以选择 Surface 输入
            surface = videoEncodec.createInputSurface();  // Surface 输入模式从 API 19 启用比较好

        } catch (IOException e) {
            e.printStackTrace();
            videoEncodec = null;
            videoFormat = null;
            videoBufferinfo = null;
        }
    }

    private void initAudioEncodec(String mimeType, int sampleRate, int channelCount) {
        try {
            this.sampleRate = sampleRate;
            audioBufferinfo = new MediaCodec.BufferInfo();

            // MediaCodec 编码Audio，
            // 必须设置MediaFormat->KEY_MINE、KEY_BIT_RATE、KEY_CHANNEL_COUNT及KEY_SAMPLE_RATE这四个
            audioFormat = MediaFormat.createAudioFormat(mimeType, sampleRate, channelCount);
            audioFormat.setInteger(MediaFormat.KEY_BIT_RATE, 96000);

            audioFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
            audioFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 4096 * 10);

            // 通过类型创建音频编码器
            audioEncodec = MediaCodec.createEncoderByType(mimeType);
            audioEncodec.configure(audioFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        } catch (IOException e) {
            e.printStackTrace();
            audioBufferinfo = null;
            audioFormat = null;
            audioEncodec = null;
        }
    }

    // 编码音频数据 // 将pcm原始数据 输入到队列,以便推流时获取
    public void putPCMData(byte[] buffer, int size) {
        if (audioEncodecThread != null && !audioEncodecThread.isExit && buffer != null && size > 0) {
            // 从输入流队列中取数据进行编码操作
            int inputBufferindex = audioEncodec.dequeueInputBuffer(0);
            if (inputBufferindex >= 0) {
                // 获取需要编码数据的输入流队列，返回的是一个ByteBuffer数组
                ByteBuffer byteBuffer = audioEncodec.getInputBuffers()[inputBufferindex];
                byteBuffer.clear();
                byteBuffer.put(buffer);
                long pts = getAudioPts(size, sampleRate);
                //输入流入队列
                audioEncodec.queueInputBuffer(inputBufferindex, 0, size, pts, 0);
            }
        }
    }

    // OpenGL ES 线程 //Egl线程
    static class WlEGLMediaThread extends Thread {
        private WeakReference<WlBasePushEncoder> encoder;
        private EglHelper eglHelper;
        private Object object;

        private boolean isExit = false;
        private boolean isCreate = false;
        private boolean isChange = false;
        private boolean isStart = false;

        public WlEGLMediaThread(WeakReference<WlBasePushEncoder> encoder) {
            this.encoder = encoder;
        }

        @Override
        public void run() {
            super.run();
            isExit = false;
            isStart = false;
            object = new Object();

            eglHelper = new EglHelper();    // 实例化 EglHelper
            eglHelper.initEgl(encoder.get().surface, encoder.get().eglContext); // 初始化egl环境

            while (true) {
                if (isExit) {
                    release();
                    break;
                }

                if (isStart) {
                    if (encoder.get().mRenderMode == RENDERMODE_WHEN_DIRTY) {
                        synchronized (object) {
                            try {
                                object.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    } else if (encoder.get().mRenderMode == RENDERMODE_CONTINUOUSLY) {
                        try {
                            Thread.sleep(1000 / 60);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        throw new RuntimeException("mRenderMode is wrong value");
                    }
                }

                onCreate();
                onChange(encoder.get().width, encoder.get().height);
                onDraw();

                isStart = true;
            }
        }

        private void onCreate() {
            if (isCreate && encoder.get().wlGLRender != null) {
                isCreate = false;
                encoder.get().wlGLRender.onSurfaceCreated();
            }
        }

        private void onChange(int width, int height) {
            if (isChange && encoder.get().wlGLRender != null) {
                isChange = false;
                encoder.get().wlGLRender.onSurfaceChanged(width, height);
            }
        }

        private void onDraw() {
            if (encoder.get().wlGLRender != null && eglHelper != null) {
                encoder.get().wlGLRender.onDrawFrame();
                if (!isStart) {
                    encoder.get().wlGLRender.onDrawFrame();
                }
                eglHelper.swapBuffers();
            }
        }

        private void requestRender() {
            if (object != null) {
                synchronized (object) {
                    object.notifyAll();
                }
            }
        }

        public void onDestory() {
            isExit = true;
            requestRender();
        }

        public void release() {
            if (eglHelper != null) {
                eglHelper.destoryEgl();
                eglHelper = null;
                object = null;
                encoder = null;
            }
        }
    }

    static class VideoEncodecThread extends Thread {
        private WeakReference<WlBasePushEncoder> encoder;
        private boolean isExit;
        private MediaCodec videoEncodec;
        private MediaCodec.BufferInfo videoBufferinfo;

        private long pts;
        private byte[] sps;
        private byte[] pps;
        private boolean keyFrame = false;

        public VideoEncodecThread(WeakReference<WlBasePushEncoder> encoder) {
            this.encoder = encoder;
            videoEncodec = encoder.get().videoEncodec;
            videoBufferinfo = encoder.get().videoBufferinfo;
        }

        @Override
        public void run() {
            super.run();
            pts = 0;
            isExit = false;
            videoEncodec.start();
            while (true) {
                if (isExit) {
                    videoEncodec.stop();
                    videoEncodec.release();
                    videoEncodec = null;
                    Log.d("lcr", "录制完成");
                    break;
                }

                //BufferInfo对象，用于存储ByteBuffer的信息
                // 从输出队列中取出编码操作之后的数据
                int outputBufferIndex = videoEncodec.dequeueOutputBuffer(videoBufferinfo, 0);
                keyFrame = false;
                // 输出格式改变
                if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    Log.d("lcr", "==========INFO_OUTPUT_FORMAT_CHANGED");
                    ByteBuffer spsb = videoEncodec.getOutputFormat().getByteBuffer("csd-0");
                    sps = new byte[spsb.remaining()];
                    spsb.get(sps, 0, sps.length);

                    ByteBuffer ppsb = videoEncodec.getOutputFormat().getByteBuffer("csd-1");
                    pps = new byte[ppsb.remaining()];
                    ppsb.get(pps, 0, pps.length);

                    // Log.d("lcr", "sps:" + byteToHex(sps));
                    // Log.d("lcr", "pps:" + byteToHex(pps));
                } else {
                    while (outputBufferIndex >= 0) {
                        // 获取编解码之后的数据输出流队列，返回的是一个ByteBuffer数组
                        ByteBuffer outputBuffer = videoEncodec.getOutputBuffers()[outputBufferIndex];
                        outputBuffer.position(videoBufferinfo.offset);
                        outputBuffer.limit(videoBufferinfo.offset + videoBufferinfo.size);
                        //
                        if (pts == 0) {
                            pts = videoBufferinfo.presentationTimeUs;
                        }
                        videoBufferinfo.presentationTimeUs = videoBufferinfo.presentationTimeUs - pts;

                        byte[] data = new byte[outputBuffer.remaining()];
                        outputBuffer.get(data, 0, data.length);
                        //Log.d("lcr", "data:" + byteToHex(data));

                        if (videoBufferinfo.flags == MediaCodec.BUFFER_FLAG_KEY_FRAME) { // 关键帧
                            keyFrame = true; // 关键帧
                            // 如果 是关键帧，则在发关键帧之前称发送sps和pps。用户中途进入，可顺利解码
                            if (encoder.get().onMediaInfoListener != null) {
                                encoder.get().onMediaInfoListener.onSPSPPSInfo(sps, pps);
                            }
                        }

                        if (encoder.get().onMediaInfoListener != null) {
                            //将数据及是否是关键帧返回到推流中
                            encoder.get().onMediaInfoListener.onVideoInfo(data, keyFrame);
                            encoder.get().onMediaInfoListener.onMediaTime((int) (videoBufferinfo.presentationTimeUs / 1000000));
                        }
                        //处理完成，释放ByteBuffer数据
                        videoEncodec.releaseOutputBuffer(outputBufferIndex, false);

                        // 从输出队列中取出编码操作之后的数据
                        outputBufferIndex = videoEncodec.dequeueOutputBuffer(videoBufferinfo, 0);
                    }
                }
            }
        }

        public void exit() {
            isExit = true;
        }
    }

    static class AudioEncodecThread extends Thread {
        private WeakReference<WlBasePushEncoder> encoder;
        private boolean isExit;

        private MediaCodec audioEncodec;
        private MediaCodec.BufferInfo bufferInfo;
        private long pts;

        public AudioEncodecThread(WeakReference<WlBasePushEncoder> encoder) {
            this.encoder = encoder;
            audioEncodec = encoder.get().audioEncodec;
            bufferInfo = encoder.get().audioBufferinfo;
        }

        @Override
        public void run() {
            super.run();
            pts = 0;
            isExit = false;
            audioEncodec.start();
            while (true) {
                if (isExit) {
                    //
                    audioEncodec.stop();
                    audioEncodec.release();
                    audioEncodec = null;
                    break;
                }

                int outputBufferIndex = audioEncodec.dequeueOutputBuffer(bufferInfo, 0);
                if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                } else {
                    while (outputBufferIndex >= 0) {
                        ByteBuffer outputBuffer = audioEncodec.getOutputBuffers()[outputBufferIndex];
                        outputBuffer.position(bufferInfo.offset);
                        outputBuffer.limit(bufferInfo.offset + bufferInfo.size);
                        if (pts == 0) {
                            pts = bufferInfo.presentationTimeUs;
                        }
                        bufferInfo.presentationTimeUs = bufferInfo.presentationTimeUs - pts;

                        byte[] data = new byte[outputBuffer.remaining()];
                        outputBuffer.get(data, 0, data.length);
                        if (encoder.get().onMediaInfoListener != null) {
                            encoder.get().onMediaInfoListener.onAudioInfo(data); // 编码操作之后的数据
                        }

                        audioEncodec.releaseOutputBuffer(outputBufferIndex, false);
                        outputBufferIndex = audioEncodec.dequeueOutputBuffer(bufferInfo, 0);
                    }
                }
            }
        }

        public void exit() {
            isExit = true;
        }
    }

    public interface OnMediaInfoListener {
        void onMediaTime(int times);

        void onSPSPPSInfo(byte[] sps, byte[] pps);

        void onVideoInfo(byte[] data, boolean keyframe);

        void onAudioInfo(byte[] data);
    }

    private long getAudioPts(int size, int sampleRate) {
        audioPts += (long) (1.0 * size / (sampleRate * 2 * 2) * 1000000.0);

        return audioPts;
    }

    public static String byteToHex(byte[] bytes) {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(bytes[i]);
            if (hex.length() == 1) {
                stringBuffer.append("0" + hex);
            } else {
                stringBuffer.append(hex);
            }
            if (i > 20) {
                break;
            }
        }
        return stringBuffer.toString();
    }
}
