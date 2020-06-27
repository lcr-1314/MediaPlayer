package com.android.mymusic;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;

import com.android.myplayer.WlTimeInfoBean;
import com.android.myplayer.listener.WlOnCompleteListener;
import com.android.myplayer.listener.WlOnErrorListener;
import com.android.myplayer.listener.WlOnLoadListener;
import com.android.myplayer.listener.WlOnParparedListener;
import com.android.myplayer.listener.WlOnPauseResumeListener;
import com.android.myplayer.listener.WlOnTimeInfoListener;
import com.android.myplayer.muteenum.MuteEnum;
import com.android.myplayer.opengl.WlGLSurfaceView;
import com.android.myplayer.player.WlPlayer;
import com.android.myplayer.util.WlTimeUtil;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = "MainActivity";

    private WlPlayer wlPlayer;
    private TextView tvTime;
	private WlGLSurfaceView wlGLSurfaceView;
    private SeekBar seekBar_seek;
    private int position = 0;
    private boolean isSeekBar = false;

    private SeekBar seekBar_volume;
    private TextView tv_volume;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        tvTime = findViewById(R.id.tv_time);
		wlGLSurfaceView = findViewById(R.id.wlglsurfaceview);
		seekBar_seek = findViewById(R.id.seekBar_seek);

		wlPlayer = new WlPlayer();
        wlPlayer.setWlGLSurfaceView(wlGLSurfaceView);

        wlPlayer.setWlOnParparedListener(new WlOnParparedListener() {
            @Override
            public void onParpared() {
                Log.d(TAG, "准备完成，可以开始播放了.");
                wlPlayer.start();       // 准备完成后，开始进入播放
            }
        });

        wlPlayer.setWlOnLoadListener(new WlOnLoadListener() {
            @Override
            public void onLoad(boolean load) {
                if (load) {
                    Log.d(TAG, "加载中...");
                } else {
                    Log.d(TAG, "播放中...");
                }
            }
        });

        wlPlayer.setWlOnPauseResumeListener(new WlOnPauseResumeListener() {
            @Override
            public void onPause(boolean pause) {
                if (pause) {
                    Log.d(TAG, "暂停中...");
                } else {
                    Log.d(TAG, "播放中...");
                }
            }
        });

        wlPlayer.setWlOnTimeInfoListener(new WlOnTimeInfoListener() {
            @Override
            public void onTimeInfo(WlTimeInfoBean wlTimeInfoBean) {
                Message message = Message.obtain();
                message.what = 1;
                message.obj = wlTimeInfoBean;
                handler.sendMessage(message);
            }
        });

        wlPlayer.setWlOnErrorListener(new WlOnErrorListener() {
            @Override
            public void onError(int code, String msg) {
                Log.d(TAG, "code:" + code + ", msg:" + msg);
            }
        });

        wlPlayer.setWlOnCompleteListener(new WlOnCompleteListener() {
            @Override
            public void onComplete() {
                Log.d(TAG, "播放完成了");
                playIndex++;
                if (playIndex >= audiopaths.size()) {
                    playIndex = 0;
                }
                wlPlayer.playNext(audiopaths.get(playIndex));
            }
        });

		seekBar_seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (wlPlayer.getDuration() > 0 && isSeekBar) {
                    position = wlPlayer.getDuration() * progress / 100;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isSeekBar = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d(TAG, "====lcr====position:" + position);
                wlPlayer.seek(position);
                isSeekBar = false;
            }
        });
		
		tv_volume = findViewById(R.id.tv_volume);
        seekBar_volume = findViewById(R.id.seekBar_volume);
        wlPlayer.setVolume(50);
        wlPlayer.setMute(MuteEnum.MUTE_LEFT);
        tv_volume.setText("音量:" + wlPlayer.getVolumePercent() + "%");
        seekBar_volume.setProgress(wlPlayer.getVolumePercent());
		
		seekBar_volume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                wlPlayer.setVolume(progress);
                tv_volume.setText("音量：" + progress + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    // 开始播放按钮
    public void begin(View view) {
        wlPlayer.setSource(audiopaths.get(playIndex)); // 设置播放路径
        wlPlayer.parpared();
    }

    public void pause(View view) {
        wlPlayer.pause();
    }

    public void resume(View view) {
        wlPlayer.resume();
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1: {
                    if (!isSeekBar) {
                        WlTimeInfoBean wlTimeInfoBean = (WlTimeInfoBean) msg.obj;
                        tvTime.setText(WlTimeUtil.secdsToDateFormat(wlTimeInfoBean.getTotalTime(), wlTimeInfoBean.getTotalTime())
                                + "/" + WlTimeUtil.secdsToDateFormat(wlTimeInfoBean.getCurrentTime(), wlTimeInfoBean.getTotalTime()));

                        if (!isSeekBar && wlTimeInfoBean.getTotalTime() > 0) {
                            seekBar_seek.setProgress(wlTimeInfoBean.getCurrentTime() * 100 / wlTimeInfoBean.getTotalTime());
                        }
                    }
                }
                    break;
                default:
                    break;
            }
        }
    };

    public void stop(View view) {
        wlPlayer.stop();
    }

    public void next(View view) {
//        playIndex++;
//        if(playIndex >= audiopaths.size()){
//            playIndex = 0;
//        }

        wlPlayer.playNext(audiopaths.get(playIndex));
    }

    // 左右声道及立体声
    public void left(View view) {
        wlPlayer.setMute(MuteEnum.MUTE_LEFT);
    }

    public void center(View view) {
        wlPlayer.setMute(MuteEnum.MUTE_CENTER);
    }

    public void right(View view) {
        wlPlayer.setMute(MuteEnum.MUTE_RIGHT);
    }
	
    static int playIndex = 0;
    static List<String> audiopaths = new ArrayList<>();
    static {
        // audiopaths.add("/storage/emulated/0/Pictures/[电影天堂www.dy2018.com]魁拔2HD.rmvb");

        audiopaths.add("/storage/emulated/0/Music/[电影天堂www.dy2018.com]魁拔2HD.rmvb");

        audiopaths.add("/storage/emulated/0/Music/myvideo.mp4");
        //audiopaths.add("/storage/emulated/0/Music/sintel_640_360.yuv");
        audiopaths.add("/storage/emulated/0/DJI/dji.go.v5/videoguide/video_first_lanch_wm160.mp4");
        audiopaths.add("/storage/emulated/0/DJI/dji.go.v5/mpnewbieguide/videoguide/mp_newbie_guide_step_3.mp4");
        audiopaths.add("/storage/emulated/0/DJI/dji.go.v5/DJI FLY/Video/2020_04_28_18_31_48_Cache.mp4");
        audiopaths.add("/storage/emulated/0/DJI/dji.go.v5/DJI FLY/Video/2020_04_29_18_12_30_Cache.mp4");
        audiopaths.add("/storage/emulated/0/Music/[电影天堂www.dy2018.com]魁拔BD1280.rmvb");

        audiopaths.add("/storage/emulated/0/qqmusic/song/校长 - 带你去旅行 [mqms2].mp3");
        audiopaths.add("/storage/emulated/0/Music/一曲相思（完整SQ版）.mp3");
        audiopaths.add("/storage/emulated/0/Music/DJ - ¿ÉÄÜ·ñ (DJÇñ×Ü)_ÁåÉùÖ®¼Òcnwav.aac");
        audiopaths.add("http://mpge.5nd.com/2015/2015-11-26/69708/1.mp3");
        audiopaths.add("http://ngcdn001.cnr.cn/live/zgzs/index.m3u8");
        audiopaths.add("rtmp://106.53.210.199/myapp/mystream");
    }
}
