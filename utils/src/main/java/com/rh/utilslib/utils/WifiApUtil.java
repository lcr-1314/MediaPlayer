package com.rh.utilslib.utils;

import android.Manifest;
import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.support.annotation.RequiresPermission;
import android.util.Log;

import java.lang.reflect.Method;

/**
 * Created by billXiang on 2018/3/28.
 */

public class WifiApUtil {
    private WifiManager mWifiManager;
    private static WifiApUtil sWifiApUtil = null;

    private WifiApUtil(Context context) {
        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    }

    public static WifiApUtil getInstance(Context context){
        if(sWifiApUtil == null){
            sWifiApUtil = new WifiApUtil(context);
        }
        return sWifiApUtil;
    }

    @RequiresPermission(allOf = {Manifest.permission.CHANGE_WIFI_STATE})
    public boolean setWifiApEnabled(WifiConfiguration wifiConfig, boolean enabled) {
        try {
            if (enabled) { // disable WiFi in any case
                mWifiManager.setWifiEnabled(false);
            }
            Method method = mWifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            return (Boolean) method.invoke(mWifiManager, wifiConfig, enabled);
        } catch (Exception e) {
            Log.e(this.getClass().toString(), "", e);
            return false;
        }
    }



    /**
     * Gets the Wi-Fi AP Configuration.
     * @return AP details in {@link WifiConfiguration}
     */
    public WifiConfiguration getWifiApConfiguration() {
        try {
            Method method = mWifiManager.getClass().getMethod("getWifiApConfiguration");
            return (WifiConfiguration) method.invoke(mWifiManager);
        } catch (Exception e) {
            Log.e(this.getClass().toString(), "", e);
            return null;
        }
    }

    /**
     * Sets the Wi-Fi AP Configuration.
     * @return {@code true} if the operation succeeded, {@code false} otherwise
     */
    public boolean setWifiApConfiguration(WifiConfiguration wifiConfig) {
        try {
            Method method = mWifiManager.getClass().getMethod("setWifiApConfiguration", WifiConfiguration.class);
            return (Boolean) method.invoke(mWifiManager, wifiConfig);
        } catch (Exception e) {
            Log.e(this.getClass().toString(), "", e);
            return false;
        }
    }

    /***
     * WIFI热点状态
     * @return
     */
    public boolean getWifiApState(){

        int WIFI_AP_STATE_DISABLING = 10;
        int WIFI_AP_STATE_DISABLED = 11;
        int WIFI_AP_STATE_ENABLING = 12;
        int WIFI_AP_STATE_ENABLED = 13;
        int WIFI_AP_STATE_FAILED = 14;

        try {
            Method method = mWifiManager.getClass().getMethod("getWifiApState");
            Integer i = (Integer) method.invoke(mWifiManager);
            if(WIFI_AP_STATE_ENABLED == i){
                LogUtil.d(WifiApUtil.class.getSimpleName(),"WIFI_AP_STATE_ENABLED:true");
                return true;
            }
            LogUtil.d(WifiApUtil.class.getSimpleName(),"WIFI_AP_STATE_ENABLED:false");
            return false;
        } catch (Exception e) {
            Log.e(this.getClass().toString(), "", e);
            return false;
        }
    }

}
