package com.android.wllivepusher;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.wllivepusher.camera.WlCameraView;
import com.android.wllivepusher.encodec.WlBaseMediaEncoder;
import com.android.wllivepusher.encodec.WlMediaEncodec;
import com.ywl5320.libmusic.WlMusic;
import com.ywl5320.listener.OnCompleteListener;
import com.ywl5320.listener.OnPreparedListener;
import com.ywl5320.listener.OnShowPcmDataListener;

public class VideoActivity extends AppCompatActivity {
    private WlCameraView wlCameraView;
    private Button btnRecord;

    private WlMediaEncodec wlMediaEncodec;
    private WlMusic wlMusic;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        wlCameraView = findViewById(R.id.cameraview);
        btnRecord = findViewById(R.id.btn_record);

        wlMusic = WlMusic.getInstance();
        wlMusic.setCallBackPcmData(true);
        wlMusic.setOnPreparedListener(new OnPreparedListener() {
            @Override
            public void onPrepared() {
                wlMusic.playCutAudio(39, 60);
            }
        });

        // 停止录制
        wlMusic.setOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete() {
                if (wlMediaEncodec != null) {
                    wlMediaEncodec.stopRecord();
                    wlMediaEncodec = null;

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            btnRecord.setText("开始录制");
                        }
                    });
                }
            }
        });

        //开始录制
        wlMusic.setOnShowPcmDataListener(new OnShowPcmDataListener() {
            @Override
            public void onPcmInfo(int samplerate, int bit, int channels) {
                Log.d("lcr", "textureid is " + wlCameraView.getTextureId());
                wlMediaEncodec = new WlMediaEncodec(VideoActivity.this, wlCameraView.getTextureId());
                wlMediaEncodec.initEncodec(wlCameraView.getEglContext(),
                        Environment.getExternalStorageDirectory().getAbsolutePath() + "/wl_live_pusher.mp4", 720, 1280, samplerate, channels);
                wlMediaEncodec.setOnMediaInfoListener(new WlBaseMediaEncoder.OnMediaInfoListener() {
                    @Override
                    public void onMediaTime(int times) {
                        Log.d("lcr", "time is : " + times);
                    }
                });
                wlMediaEncodec.startRecord();
            }

            @Override
            public void onPcmData(byte[] pcmdata, int size, long clock) {
                if (wlMediaEncodec != null) {
                    wlMediaEncodec.putPCMData(pcmdata, size);
                }
            }
        });
    }

    public void record(View view) {
        if (wlMediaEncodec == null) {
            // Environment.getExternalStorageDirectory().getAbsolutePath() + "/不仅仅是喜欢.ogg");
            wlMusic.setSource("/storage/emulated/0/Music/一曲相思（完整SQ版）.mp3");
            wlMusic.prePared();
            btnRecord.setText("正在录制");
        } else {
            wlMediaEncodec.stopRecord();
            btnRecord.setText("开始录制");
            wlMediaEncodec = null;
            wlMusic.stop();
        }
    }
}
