package com.rh.utilslib.utils;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Gravity;
import android.widget.Toast;

import com.rh.utilslib.UtilsLib;

/**
 * ToastUtils.java
 * 解决单个弹出的问题。
 *
 * @author Wilson
 * @description toast显示工具
 * @date 2015/10/26
 * @modifier
 */
public class ToastUtils {

    public static Toast sToast;

    private static Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0x01:
                    showToast((String) msg.obj, msg.arg1);
                    break;
            }
        }
    };

    private static void showToast(String msg, int duration) {
        handler.removeCallbacks(runnable);
        if (sToast != null) {
            sToast.setText(msg);
            sToast.setDuration(duration);
        } else {
            sToast = Toast.makeText(UtilsLib.getInstance(), msg, duration);
            sToast.setGravity(Gravity.BOTTOM, 0, 100);
        }
        int durationTime;
        if (duration == Toast.LENGTH_LONG) {
            durationTime = 3000;
        } else {
            durationTime = 1500;
        }
        handler.postDelayed(runnable, durationTime);
        sToast.show();
    }

    private static Runnable runnable = new Runnable() {
        public void run() {
            sToast.cancel();
        }
    };

    public static void show(String msg, int duration) {
        if (Looper.getMainLooper().getThread().getId() == Thread.currentThread().getId()) {
            showToast(msg, duration);
        } else {
            Message message = handler.obtainMessage();
            message.what = 0x01;
            message.obj = msg;
            message.arg1 = duration;
            handler.sendMessage(message);
        }

    }

    public static void show(int msg, int duration) {
        show(UtilsLib.getInstance().getResources().getString(msg), duration);
    }

    public static void show(int msg) {
        show(UtilsLib.getInstance().getResources().getString(msg), Toast.LENGTH_LONG);
    }

    public static void show(String msg) {
        show(msg, Toast.LENGTH_LONG);
    }
}
