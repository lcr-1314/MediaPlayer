package com.android.wllivepusher;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.wllivepusher.camera.WlCameraView;
import com.android.wllivepusher.push.WlBasePushEncoder;
import com.android.wllivepusher.push.WlConnectListenr;
import com.android.wllivepusher.push.WlPushEncodec;
import com.android.wllivepusher.push.WlPushVideo;

public class LivePushActivity extends AppCompatActivity {
    private final static String TAG = "LivePushActivity";
    private WlPushVideo wlPushVideo;
    private WlCameraView wlCameraView;
    private boolean start = false;
    private WlPushEncodec wlPushEncodec;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_livepush);

        Log.d(TAG, "===================onCreate!");
        wlCameraView = findViewById(R.id.cameraview);   // 初始化 WlCameraView 对象  // 打开摄像头预览

        wlPushVideo = new WlPushVideo();                // 视频推流
        // 监听RTMP连接nginx服务器的结果
        wlPushVideo.setWlConnectListenr(new WlConnectListenr() {
            @Override
            public void onConnecting() {
                Log.d("lcr", "链接服务器中..");
            }

            @Override
            public void onConnectSuccess() {
                Log.d("lcr", "链接服务器成功，可以开始推流了");
                wlPushEncodec = new WlPushEncodec(LivePushActivity.this, wlCameraView.getTextureId());
                // 初始解码器。
                wlPushEncodec.initEncodec(wlCameraView.getEglContext(), 720 / 2, 1280 / 2);
                wlPushEncodec.startRecord();
                wlPushEncodec.setOnMediaInfoListener(new WlBasePushEncoder.OnMediaInfoListener() {
                    @Override
                    public void onMediaTime(int times) {
                    }

                    @Override
                    public void onSPSPPSInfo(byte[] sps, byte[] pps) {
                        //推流 序列参数集和图像参数集
                        wlPushVideo.pushSPSPPS(sps, pps);
                    }

                    @Override
                    public void onVideoInfo(byte[] data, boolean keyframe) {
                        // 接收到视频数据，推流视频数据
                        wlPushVideo.pushVideoData(data, keyframe);  // 推视频数据
                    }

                    @Override
                    public void onAudioInfo(byte[] data) {
                        // 接收到音频数据，推流音频数据
                        wlPushVideo.pushAudioData(data);            // 推音频数据
                    }
                });
            }

            @Override
            public void onConnectFail(String msg) {
                Log.d("lcr", msg);
            }
        });
    }

    // 点击 开始推流 / 停止推流
    public void startpush(View view) {
        start = !start;
        if (start) {
            wlPushVideo.initLivePush("rtmp://106.53.210.199/myapp/mystream");
            // wlPushVideo.initLivePush("rtmp://192.168.1.100/myapp/mystream");
        } else {
            if (wlPushEncodec != null) {
                wlPushEncodec.stopRecord();
                wlPushVideo.stopPush();
                wlPushEncodec = null;
            }
        }
    }
}
