package com.android.myplayer.player;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;

import com.android.myplayer.WlTimeInfoBean;
import com.android.myplayer.listener.WlOnCompleteListener;
import com.android.myplayer.listener.WlOnErrorListener;
import com.android.myplayer.listener.WlOnLoadListener;
import com.android.myplayer.listener.WlOnParparedListener;
import com.android.myplayer.listener.WlOnPauseResumeListener;
import com.android.myplayer.listener.WlOnTimeInfoListener;
import com.android.myplayer.muteenum.MuteEnum;
import com.android.myplayer.opengl.WlGLSurfaceView;
import com.android.myplayer.opengl.WlRender;
import com.android.myplayer.util.WlVideoSupportUitl;

import java.nio.ByteBuffer;

public class WlPlayer {
    private final static String TAG = "WlPlayer";

    private static String source;
    private static WlTimeInfoBean wlTimeInfoBean;
    private static boolean playNext = false;

    private static int volumePercent = 100;
    private MuteEnum muteEnum = MuteEnum.MUTE_CENTER;

    private WlOnParparedListener wlOnParparedListener;
    private WlOnLoadListener wlOnLoadListener;
    private WlOnPauseResumeListener wlOnPauseResumeListener;
    private WlOnTimeInfoListener wlOnTimeInfoListener;
    private WlOnErrorListener wlOnErrorListener;
    private WlOnCompleteListener wlOnCompleteListener;

    private WlGLSurfaceView wlGLSurfaceView;
    private static int duration = -1;

    private MediaFormat mediaFormat;
    private MediaCodec mediaCodec;
    private Surface surface;
    private MediaCodec.BufferInfo info;

    public WlPlayer() {
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setWlGLSurfaceView(WlGLSurfaceView wlGLSurfaceView) {
        this.wlGLSurfaceView = wlGLSurfaceView;
        wlGLSurfaceView.getWlRender().setOnSurfaceCreateListener(new WlRender.OnSurfaceCreateListener() {
            @Override
            public void onSurfaceCreate(Surface s) {
                if (surface == null) {
                    surface = s;
                    Log.d(TAG, "onSurfaceCreate");
                }
            }
        });
    }

    /**
     * 设置准备接口回调
     * @param wlOnParparedListener
     */
    public void setWlOnParparedListener(WlOnParparedListener wlOnParparedListener) {
        this.wlOnParparedListener = wlOnParparedListener;
    }

    public void setWlOnLoadListener(WlOnLoadListener wlOnLoadListener) {
        this.wlOnLoadListener = wlOnLoadListener;
    }

    public void setWlOnPauseResumeListener(WlOnPauseResumeListener wlOnPauseResumeListener) {
        this.wlOnPauseResumeListener = wlOnPauseResumeListener;
    }

    public void setWlOnTimeInfoListener(WlOnTimeInfoListener wlOnTimeInfoListener) {
        this.wlOnTimeInfoListener = wlOnTimeInfoListener;
    }

    public void setWlOnErrorListener(WlOnErrorListener wlOnErrorListener) {
        this.wlOnErrorListener = wlOnErrorListener;
    }

    public void setWlOnCompleteListener(WlOnCompleteListener wlOnCompleteListener) {
        this.wlOnCompleteListener = wlOnCompleteListener;
    }

    public void parpared() {
        if (TextUtils.isEmpty(source)) {
            Log.d(TAG, "source not be empty");
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                n_parpared(source);
            }
        }).start();
    }

    public void start() {
        if (TextUtils.isEmpty(source)) {
            Log.d(TAG, "source can not be empty!");
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                setVolume(volumePercent);   // 设置声音
                setMute(muteEnum);          // 设置左右声道及立体声
                n_start();                  // 进入播放
            }
        }).start();
    }

    public void pause() {
        n_pause();

        if (wlOnPauseResumeListener != null) {
            wlOnPauseResumeListener.onPause(true);
        }
    }

    public void resume() {
        n_resume();
        if (wlOnPauseResumeListener != null) {
            wlOnPauseResumeListener.onPause(false);
        }
    }

    public void stop() {
        duration = -1;
        wlTimeInfoBean = null;

        new Thread(new Runnable() {
            @Override
            public void run() {
                n_stop();
                releaseMediacodec();
            }
        }).start();
    }

    public void seek(int seek) {
        n_seek(seek);
    }

    public void playNext(String url) {
        source = url;
        playNext = true;
        stop();
    }

    public int getDuration() {
        if (duration < 0) {
            duration = n_duration();
        }
        return duration;
    }

    public void onCallParpared() {
        if (wlOnParparedListener != null) {
            wlOnParparedListener.onParpared();
        }
    }

    public void onCallLoad(boolean load) {
        if (wlOnLoadListener != null) {
            wlOnLoadListener.onLoad(load);
        }
    }

    public void onCallTimeInfo(int currentTime, int totalTime) {
        if (wlOnTimeInfoListener != null) {
            if (wlTimeInfoBean == null) {
                wlTimeInfoBean = new WlTimeInfoBean();
            }
            duration = totalTime;
            wlTimeInfoBean.setCurrentTime(currentTime);
            wlTimeInfoBean.setTotalTime(totalTime);

            wlOnTimeInfoListener.onTimeInfo(wlTimeInfoBean);
        }
    }

    public void onCallError(int code, String msg) {
        if (wlOnErrorListener != null) {
            stop();
            wlOnErrorListener.onError(code, msg);
        }
    }

    public void onCallComplete() {
        if (wlOnCompleteListener != null) {
            stop();
            wlOnCompleteListener.onComplete();
        }
    }

    public void onCallNext() {
        if (playNext) {
            playNext = false;
            parpared();
        }
    }

    public void onCallRenderYUV(int width, int height, byte[] y, byte[] u, byte[] v) {
        Log.d(TAG, "获取到视频的yuv数据");
        if (wlGLSurfaceView != null) {
            wlGLSurfaceView.getWlRender().setRenderType(WlRender.RENDER_YUV);
            wlGLSurfaceView.setYUVData(width, height, y, u, v);
        }
    }

    public boolean onCallIsSupportMediaCodec(String ffcodecname) {
        return WlVideoSupportUitl.isSupportCodec(ffcodecname);
    }

    public void initMediaCodec(String codecName, int width, int height, byte[] csd_0, byte[] csd_1) {
        if (surface != null) {
            try {
                wlGLSurfaceView.getWlRender().setRenderType(WlRender.RENDER_MEDIACODEC);
                String mime = WlVideoSupportUitl.findVideoCodecName(codecName);
                mediaFormat = MediaFormat.createVideoFormat(mime, width, height);

                mediaFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, width * height);
                mediaFormat.setByteBuffer("csd-0", ByteBuffer.wrap(csd_0));
                mediaFormat.setByteBuffer("csd-1", ByteBuffer.wrap(csd_1));
                //Log.d(TAG, "============mediaFormat：" + mediaFormat.toString());

                mediaCodec = MediaCodec.createDecoderByType(mime);

                info = new MediaCodec.BufferInfo();
                mediaCodec.configure(mediaFormat, surface, null, 0);
                mediaCodec.start();

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            if (wlOnErrorListener != null) {
                wlOnErrorListener.onError(2001, "surface is null");
            }
        }
    }

    public void decodeAVPacket(int datasize, byte[] data) {
        if (surface != null && datasize > 0 && data != null && mediaCodec != null) {
            try {
                int inputBufferIndex = mediaCodec.dequeueInputBuffer(10);
                if (inputBufferIndex >= 0) {
                    ByteBuffer byteBuffer = mediaCodec.getInputBuffers()[inputBufferIndex];
                    byteBuffer.clear();
                    byteBuffer.put(data);
                    mediaCodec.queueInputBuffer(inputBufferIndex, 0, datasize, 0, 0);
                }
                int outputBufferIndex = mediaCodec.dequeueOutputBuffer(info, 10);
                while (outputBufferIndex >= 0) {
                    mediaCodec.releaseOutputBuffer(outputBufferIndex, true);
                    outputBufferIndex = mediaCodec.dequeueOutputBuffer(info, 10);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void releaseMediacodec() {
        if (mediaCodec != null) {
            try {
                mediaCodec.flush();
                mediaCodec.stop();
                mediaCodec.release();
            } catch (Exception e) {
                e.printStackTrace();
            }

            mediaCodec = null;
            mediaFormat = null;
            info = null;
        }
    }

    public void setVolume(int percent) {
        if (percent >= 0 && percent <= 100) {
            volumePercent = percent;
            n_volume(percent);
        }
    }

    public void setMute(MuteEnum mute) {
        muteEnum = mute;
        n_mute(mute.getValue());
    }

    public int getVolumePercent() {
        return volumePercent;
    }

    private native void n_parpared(String source);
    private native void n_start();
    private native void n_pause();
    private native void n_resume();
    private native void n_stop();
    private native void n_seek(int seek);
    private native int n_duration();
    private native void n_volume(int percent);
    private native void n_mute(int mute);

    static {
        System.loadLibrary("native-lib");
    }
}
