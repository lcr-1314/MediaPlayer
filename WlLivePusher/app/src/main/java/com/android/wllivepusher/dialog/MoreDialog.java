package com.android.wllivepusher.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.android.wllivepusher.MainActivity;
import com.android.wllivepusher.R;
import com.android.wllivepusher.util.Constant;

public class MoreDialog extends Dialog {
    private final static String TAG = "MoreDialog";
    private Context mContext = null;
    private Handler mHanlder = null;
    private Window mDialogWindow;

    private int mRecordIndex = Constant.HANDLER_MSG_AUDIO_PCM;

    public MoreDialog(Context context, int themeResId, Handler handler) {
        super(context, themeResId);
        this.mContext = context;
        this.mHanlder = handler;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.more_dialog);

        // 空白处能取消动画
        setCanceledOnTouchOutside(true);
        mDialogWindow = this.getWindow();
        WindowManager.LayoutParams lp = mDialogWindow.getAttributes();
        lp.flags = WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
        lp.gravity = Gravity.CENTER;

        lp.width = (int) (MainActivity.screenWidth * 0.85f);
        lp.height = (int) (MainActivity.screenHeight * 0.6f);

        mDialogWindow.setAttributes(lp);

        initView();
    }

    private EditText mEditText;
    private RadioGroup mRadioGroup;
    private void initView() {
        mRadioGroup = findViewById(R.id.btn_radiogroup);
        mRadioGroup.check(R.id.rbt_audio_pcm);
        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                Log.d(TAG, "===========checkedId:" + checkedId);
                switch (checkedId) {
                    case R.id.rbt_audio_pcm: {
                        mRecordIndex = Constant.HANDLER_MSG_AUDIO_PCM;
                    }
                        break;
                    case R.id.rbt_audio_wav: {
                        mRecordIndex = Constant.HANDLER_MSG_AUDIO_WAV;
                    }
                        break;
                    case R.id.rbt_video_yuv: {
                        mRecordIndex = Constant.HANDLER_MSG_VIDEO_YUV;
                    }
                        break;
                    case R.id.rbt_video_audio: {
                        mRecordIndex = Constant.HANDLER_MSG_VIDEO_AUDIO;
                    }
                        break;
                    case R.id.rbt_audio_image: {
                        mRecordIndex = Constant.HANDLER_MSG_IMAGE_AUDIO;
                    }
                        break;
                }
            }
        });

        mEditText = findViewById(R.id.et_pathname);
        mEditText.setText(System.currentTimeMillis() + "");
        findViewById(R.id.btn_start_record).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mHanlder != null) {
                    String path = mEditText.getText().toString();
                    Message msg = Message.obtain();
                    msg.what = mRecordIndex;
                    msg.obj = path;
                    mHanlder.removeMessages(mRecordIndex);
                    mHanlder.sendMessage(msg);
                    mHanlder.sendEmptyMessage(1);
                }

                MoreDialog.this.dismiss();
            }
        });

        findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MoreDialog.this.dismiss();
            }
        });
    }

    @Override
    public void dismiss() {
        super.dismiss();
        if (mHanlder != null) {
            mHanlder.removeMessages(Constant.HANDLER_MSG_MYDIALOG);
            mHanlder.sendEmptyMessage(Constant.HANDLER_MSG_MYDIALOG);
        }
    }
}
