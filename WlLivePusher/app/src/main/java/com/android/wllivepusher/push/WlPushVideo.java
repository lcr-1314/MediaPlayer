package com.android.wllivepusher.push;

import android.media.AudioFormat;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.android.wllivepusher.encodec.MyAudioPcmListener;
import com.android.wllivepusher.util.PcmToWavUtil;

import java.io.File;

public class WlPushVideo {
    private WlConnectListenr wlConnectListenr;
    static {
        System.loadLibrary("wlpush");
    }

    public void setWlConnectListenr(WlConnectListenr wlConnectListenr) {
        this.wlConnectListenr = wlConnectListenr;
    }

    private void onConnecting() {
        if (wlConnectListenr != null) {
            wlConnectListenr.onConnecting();
        }
    }

    private void onConnectSuccess() {
        if (wlConnectListenr != null) {
            wlConnectListenr.onConnectSuccess();
        }
    }

    private void onConnectFial(String msg) {
        if (wlConnectListenr != null) {
            wlConnectListenr.onConnectFail(msg);
        }
    }

    public void initLivePush(String url) {
        if (!TextUtils.isEmpty(url)) {
            initPush(url);
        }
    }

    public void pushSPSPPS(byte[] sps, byte[] pps) {
        if (sps != null && pps != null) {
            pushSPSPPS(sps, sps.length, pps, pps.length);
        }
    }

    public void pushVideoData(byte[] data, boolean keyframe) {
        if (data != null) {
            pushVideoData(data, data.length, keyframe);
        }
    }

    public void pushAudioData(byte[] data) {
        if (data != null) {
            pushAudioData(data, data.length);
        }
    }

    public void stopPush() {
        pushStop();
    }

    // audio record
    private MyAudioPcmListener myAudioPcmListener;
    public void setMyAudioPcmListener(MyAudioPcmListener myAudioPcmListener) {
        this.myAudioPcmListener = myAudioPcmListener;
    }

    private void onAudioPcmData(byte[] data, int size) {
        Log.d("lcr", "============data.length:" + data.length);
        if (myAudioPcmListener != null) {
            myAudioPcmListener.AudioPcmData(data, size);
        }
    }

    public void startRecord() {
        startRecord(Environment.getExternalStorageDirectory().getAbsolutePath() + "/wl_opensl_record.pcm");
    }

    public void stopRecorded() {
        stopRecord();

        String pcmFile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/wl_opensl_record.pcm";

        String path = System.currentTimeMillis() + ".wav";
        addHeadData(path, pcmFile);
    }

    private File handlerWavFile;
    private   void addHeadData(String wavPath, String pcmFile) {
        handlerWavFile = new File(Environment.getExternalStorageDirectory() + File.separator + wavPath);
        PcmToWavUtil pcmToWavUtil = new PcmToWavUtil(44100, AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT);
        pcmToWavUtil.pcmToWav(pcmFile, handlerWavFile.toString());
    }

    private native void initPush(String pushUrl);

    private native void pushSPSPPS(byte[] sps, int sps_len, byte[] pps, int pps_len);

    private native void pushVideoData(byte[] data, int data_len, boolean keyframe);

    private native void pushAudioData(byte[] data, int data_len);

    private native void pushStop();

    public native void startRecord(String path);
    public native void stopRecord();
}
