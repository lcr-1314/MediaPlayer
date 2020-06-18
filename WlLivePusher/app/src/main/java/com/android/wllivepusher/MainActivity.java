package com.android.wllivepusher;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.renderscript.RenderScript;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import com.android.wllivepusher.camera.WlCameraView;
import com.android.wllivepusher.dialog.MoreDialog;
import com.android.wllivepusher.encodec.MyAudioPcmListener;
import com.android.wllivepusher.encodec.WlBaseMediaEncoder;
import com.android.wllivepusher.encodec.WlMediaEncodec;
import com.android.wllivepusher.push.WlAudioRecordUitl;
import com.android.wllivepusher.push.WlBasePushEncoder;
import com.android.wllivepusher.push.WlConnectListenr;
import com.android.wllivepusher.push.WlPushEncodec;
import com.android.wllivepusher.push.WlPushVideo;
import com.android.wllivepusher.util.Constant;
import com.android.wllivepusher.util.DisplayUtil;
import com.ywl5320.libmusic.WlMusic;
import com.ywl5320.listener.OnCompleteListener;
import com.ywl5320.listener.OnPreparedListener;
import com.ywl5320.listener.OnShowPcmDataListener;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener {
    private final static String TAG = "MainActivity";

    static {
        System.loadLibrary("wlpush");
    }

    public static int screenWidth;
    public static int screenHeight;

    private WlCameraView wlCameraView;
    private LinearLayout mBtnLayout;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case Constant.HANDLER_MSG_BTN_HIDE: {
                    mBtnLayout.setVisibility(View.GONE);
                }
                    break;
                case Constant.HANDLER_MSG_BTN_SHOW:{
                    mBtnLayout.setVisibility(View.VISIBLE);
                    Log.d(TAG, "==============HANDLER_MSG_BTN_SHOW");
                }
                    break;
                case Constant.HANDLER_MSG_MYDIALOG: {
                    if (mDialog != null) {
                        mDialog.dismiss();
                        mDialog = null;
                    }
                }
                    break;
                case Constant.HANDLER_MSG_AUDIO_PCM: {
                    Log.d(TAG, "==============HANDLER_MSG_AUDIO_PCM");
                    isRecord = true;
                    String Path = (String) msg.obj;
                    wlAudioRecordUitl.startRecord(Path, true, false);
                }
                    break;
                case Constant.HANDLER_MSG_AUDIO_WAV: {
                    Log.d(TAG, "==============HANDLER_MSG_AUDIO_WAV");
                    isRecord = true;
                    String Path = (String) msg.obj;
                    wlAudioRecordUitl.startRecord(Path, true, true);
                }
                    break;
                case Constant.HANDLER_MSG_VIDEO_YUV: {
                    Log.d(TAG, "==============HANDLER_MSG_VIDEO_YUV");
                }
                    break;
                case Constant.HANDLER_MSG_VIDEO_AUDIO: {
                    Log.d(TAG, "==============HANDLER_MSG_VIDEO_AUDIO");
                }
                    break;
                case Constant.HANDLER_MSG_IMAGE_AUDIO: {
                    Log.d(TAG, "==============HANDLER_MSG_IMAGE_AUDIO");
                }
                    break;
                default:
                    break;
            }
        }
    };

    private boolean isRecord = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        screenWidth = DisplayUtil.getScreenWidth(this);
        screenHeight = DisplayUtil.getScreenHeight(this);

        wlCameraView = findViewById(R.id.cameraview);
        wlCameraView.setOnTouchListener(this);
        mBtnLayout = findViewById(R.id.ll_btnlayout);

        initVideo();
        initPush();

        mHandler.removeMessages(Constant.HANDLER_MSG_BTN_HIDE);
        mHandler.sendEmptyMessageDelayed(Constant.HANDLER_MSG_BTN_HIDE, 1000 * 3);

        final Looper looper = Looper.myLooper();

        Runnable runnable1 = new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                handler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        super.handleMessage(msg);
                        switch (msg.what) {
                            case 0:{
                                Log.e("Data", "handleMessage: " + msg.what + " Thread:" + Thread.currentThread());
                                break;
                            }
                            case 1:{
                                Log.e("Data", "handleMessage: " + msg.what + " Thread:" + Thread.currentThread());
                                break;
                            }
                            case 2:{
                                Log.e("Data", "handleMessage: " + msg.what + " Thread:" + Thread.currentThread());
                                break;
                            }
                            default:
                                break;
                        }
                    }
                };
                Looper.loop();
            }
        };
        Thread thread =new Thread(runnable1); //
        thread.setName("thread1");
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();

        Runnable runnable2 = new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                handler2 = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        super.handleMessage(msg);
                        switch (msg.what) {
                            case 0:{
                                Log.e("Data", "handleMessage: " + msg.what + " Thread:" + Thread.currentThread());
                                break;
                            }
                            case 1:{
                                Log.e("Data", "handleMessage: " + msg.what + " Thread:" + Thread.currentThread());
                                break;
                            }
                            case 2:{
                                Log.e("Data", "handleMessage: " + msg.what + " Thread:" + Thread.currentThread());
                                break;
                            }
                            default:
                                break;
                        }
                    }
                };
                Looper.loop();
            }
        };
        Thread thread2 =new Thread(runnable2); //
        thread2.setName("thread2");
        thread2.setPriority(Thread.MIN_PRIORITY);
        thread2.start();
    }
    private Handler handler;
    private Handler handler2;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        Log.d(TAG, "===============dispatchTouchEvent");
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Log.d(TAG, "===============onTouch");
        int action = event.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                Log.d(TAG, "===============onTouch->ACTION_DOWN");
                if(mDialog != null && mDialog.isShowing()){
                    mDialog.dismiss();
                    mDialog = null;
                }
                mHandler.removeMessages(Constant.HANDLER_MSG_BTN_HIDE);
                mHandler.removeMessages(Constant.HANDLER_MSG_BTN_SHOW);
                mHandler.sendEmptyMessage(Constant.HANDLER_MSG_BTN_SHOW); // 1
                break;
            case MotionEvent.ACTION_UP:
                mHandler.removeMessages(Constant.HANDLER_MSG_BTN_HIDE);
                mHandler.sendEmptyMessageDelayed(Constant.HANDLER_MSG_BTN_HIDE, 1000 * 5);
                break;
            default:
                break;
        }

        return true;
    }

    private WlAudioRecordUitl wlAudioRecordUitl;
    private void initVideo() {
        wlAudioRecordUitl = new WlAudioRecordUitl();
        wlAudioRecordUitl.setOnRecordLisener(new WlAudioRecordUitl.OnRecordLisener() {
            @Override
            public void recordByte(byte[] audioData, int readSize) {
                Log.d("lcr", "readSize is : " + readSize);
                if (wlMediaEncodec != null) {
                    wlMediaEncodec.putPCMData(audioData, readSize);
                }
            }
        });

////////////////////////////////////////////

//        wlMusic = WlMusic.getInstance();
//        wlMusic.setCallBackPcmData(true);
//        wlMusic.setOnPreparedListener(new OnPreparedListener() {
//            @Override
//            public void onPrepared() {
//                wlMusic.playCutAudio(39, 60);
//            }
//        });

//        // 停止录制
//        wlMusic.setOnCompleteListener(new OnCompleteListener() {
//            @Override
//            public void onComplete() {
//                if (wlMediaEncodec != null) {
//                    wlMediaEncodec.stopRecord();
//                    wlMediaEncodec = null;
//
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
////                            btnRecord.setText("开始录制");
//                        }
//                    });
//                }
//            }
//        });

        //开始录制
//        wlMusic.setOnShowPcmDataListener(new OnShowPcmDataListener() {
//            @Override
//            public void onPcmInfo(int samplerate, int bit, int channels) {
//                Log.d("lcr", "textureid is " + wlCameraView.getTextureId());
//                wlMediaEncodec = new WlMediaEncodec(MainActivity.this, wlCameraView.getTextureId());
//                wlMediaEncodec.initEncodec(wlCameraView.getEglContext(),
//                        Environment.getExternalStorageDirectory().getAbsolutePath() + "/wl_live_pusher.mp4",
//                        720, 1280, samplerate, channels);
//                wlMediaEncodec.setOnMediaInfoListener(new WlBaseMediaEncoder.OnMediaInfoListener() {
//                    @Override
//                    public void onMediaTime(int times) {
//                        Log.d("lcr", "time is : " + times);
//                    }
//                });
//                wlMediaEncodec.startRecord();
//            }
//
//            @Override
//            public void onPcmData(byte[] pcmdata, int size, long clock) {
//                if (wlMediaEncodec != null) {
//                    wlMediaEncodec.putPCMData(pcmdata, size);
//                }
//            }
//        });
    }

    private WlPushVideo wlPushVideo;
    private boolean start = false;
    private WlPushEncodec wlPushEncodec;
    private void initPush() {
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
                wlPushEncodec = new WlPushEncodec(MainActivity.this, wlCameraView.getTextureId());
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

        wlPushVideo.setMyAudioPcmListener(new MyAudioPcmListener() {
            @Override
            public void AudioPcmData(byte[] data, int size) {
                if (wlMediaEncodec != null) {
                    wlMediaEncodec.putPCMData(data, size);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        wlCameraView.onDestory();

        wlCameraView.post(new Runnable() {
            @Override
            public void run() {

            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d("", "=============onConfigurationChanged.");

        wlCameraView.previewAngle(MainActivity.this);
    }

    private WlMediaEncodec wlMediaEncodec;
    private WlMusic wlMusic;


    public void videorecord(View view) {
//        Intent intent = new Intent(this, VideoActivity.class);
//        startActivity(intent);

//        if (wlMediaEncodec == null) {
//            // Environment.getExternalStorageDirectory().getAbsolutePath() + "/不仅仅是喜欢.ogg");
//            wlMusic.setSource("/storage/emulated/0/Music/一曲相思（完整SQ版）.mp3");
//            wlMusic.prePared();
//
////            btnRecord.setText("正在录制");
//        } else {
//            wlMediaEncodec.stopRecord();
////            btnRecord.setText("开始录制");
//            wlMediaEncodec = null;
//            wlMusic.stop();
//        }

//        start = !start;
//        if(start) {
//            wlMediaEncodec = new WlMediaEncodec(MainActivity.this, wlCameraView.getTextureId());
//            wlMediaEncodec.initEncodec(wlCameraView.getEglContext(),
//                    Environment.getExternalStorageDirectory().getAbsolutePath() + "/wl_live_pusher.mp4",
//                    720, 1280, 44100, 2);
//
//            wlMediaEncodec.setOnMediaInfoListener(new WlBaseMediaEncoder.OnMediaInfoListener() {
//                @Override
//                public void onMediaTime(int times) {
//                    Log.d("lcr", "time is : " + times);
//                }
//            });
//            wlMediaEncodec.startRecord();
////            wlAudioRecordUitl.startRecord();
//            wlPushVideo.startRecord();
//
//        } else {
//            wlMediaEncodec.stopRecord();
//            wlMediaEncodec = null;
//            wlPushVideo.stopRecorded();
////            wlAudioRecordUitl.stopRecord();
//        }

        if (handler != null) {
            handler.sendEmptyMessage(0);
            handler.sendEmptyMessage(1);
            handler.sendEmptyMessage(2);
            Log.e("data", "==============handler");
        }
        mHandler.sendEmptyMessage(Constant.HANDLER_MSG_IMAGE_AUDIO); // 2
        mHandler.sendEmptyMessage(Constant.HANDLER_MSG_VIDEO_AUDIO); // 3
//        mHandler.sendEmptyMessage(Constant.HANDLER_MSG_IMAGE_AUDIO); // 4
//        mHandler.sendEmptyMessage(Constant.HANDLER_MSG_IMAGE_AUDIO); // 5
//        mHandler.sendEmptyMessage(Constant.HANDLER_MSG_IMAGE_AUDIO); // 6
//        mHandler.sendEmptyMessage(Constant.HANDLER_MSG_IMAGE_AUDIO); // 7
//        mHandler.sendEmptyMessage(Constant.HANDLER_MSG_IMAGE_AUDIO); // 8
//        mHandler.sendEmptyMessage(Constant.HANDLER_MSG_IMAGE_AUDIO); // 9
//        mHandler.sendEmptyMessage(Constant.HANDLER_MSG_IMAGE_AUDIO); // 10

        Message msg = Message.obtain();


        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "============mHandler.post.");
            }
        });
        if (handler2 != null) {
            handler2.sendEmptyMessage(0);
            handler2.sendEmptyMessage(1);
            handler2.sendEmptyMessage(2);
        }
    }

    public void imgvideo(View view) {
//        Intent intent = new Intent(this, ImageVideoActivity.class);
//        startActivity(intent);
    }

    public void yuvplayer(View view) {
//        Intent intent = new Intent(this, YuvActivity.class);
//        startActivity(intent);
    }

    public void livepush(View view) {
//        Intent intent = new Intent(this, LivePushActivity.class);
//        startActivity(intent);

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

    public void startRecord(View view) {
        wlPushVideo.startRecord(Environment.getExternalStorageDirectory().getAbsolutePath() + "/wl_opensl_record.pcm");
    }

    public void stopRecord(View view) {
        wlPushVideo.stopRecord();
    }

    private Dialog mDialog;
    public void more(View view) {
        if (!isRecord) {
            if (mDialog == null) {
                mDialog = new MoreDialog(MainActivity.this, R.style.dialog, mHandler);
                mDialog.show();
            } else {
                mDialog.dismiss();
                mDialog = null;
            }
        } else {
            isRecord = false;
            wlAudioRecordUitl.stopRecord();
        }
    }
}
