package com.android.wllivepusher.encodec;

public interface MyAudioPcmListener {
    void AudioPcmData(byte[] data, int size);
}
