package com.rh.utilslib.utils;

import android.Manifest;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Build;
import android.support.annotation.RequiresPermission;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;

import com.rh.utilslib.UtilsLib;

/**
 * @author yang yingwei
 * @description
 * @date 2018/3/15
 * @modify
 */

public class GsmSignalUtil {

    private static GsmSignalUtil mGsmSignalUtil = null;
    public TelephonyManager mTelephonyManager;
    public PhoneStatListener mListener;
    public Context mContext;
    private MobileSignalLevelListener mMobileSignalLevelListener = null;

    /***
     *
     */
    private GsmSignalUtil(){
        mTelephonyManager = (TelephonyManager) UtilsLib.getInstance().getSystemService(Context.TELEPHONY_SERVICE);
        //开始监听
        mListener = new PhoneStatListener();
        //监听信号强度
    }

    /***
     *
     * @return
     */
    public static GsmSignalUtil getInstance(){
        if(mGsmSignalUtil == null){
            mGsmSignalUtil = new GsmSignalUtil();
        }
        return mGsmSignalUtil;
    }

    public void setMobileSignalLevelLienster(MobileSignalLevelListener listener){
        this.mMobileSignalLevelListener = listener;
        if(mTelephonyManager != null){
            if(mListener == null){
                mListener = new PhoneStatListener();
            }
            mTelephonyManager.listen(mListener, PhoneStatListener.LISTEN_SIGNAL_STRENGTHS);
        }
    }


    private class PhoneStatListener extends PhoneStateListener {
        //获取信号强度
        @RequiresPermission(allOf = {Manifest.permission.ACCESS_NETWORK_STATE})
        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);
            //获取网络类型
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                //LogUtil.d("GsmSignalUtil","PhoneStateListener onSignalStrengthsChanged level:"+signalStrength.getLevel());
            }
            switch (AppUtils.getConnectedNetworkType()) {
                case ConnectivityManager.TYPE_MOBILE:
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                        if(mMobileSignalLevelListener != null){
                            if(AppUtils.networkAvailable()){
                                mMobileSignalLevelListener.levelChange(signalStrength.getLevel());
                            }else {
                                mMobileSignalLevelListener.levelChange(0);
                            }

                        }
                    }else {
                        if(AppUtils.networkAvailable()){
                            if(mMobileSignalLevelListener != null){
                                mMobileSignalLevelListener.levelChange(4);
                            }
                        }else {
                            if(mMobileSignalLevelListener != null){
                                mMobileSignalLevelListener.levelChange(0);
                            }
                        }
                    }
                    break;
            }
        }
    }

    public interface MobileSignalLevelListener{
        void levelChange(int level);
    }

}
