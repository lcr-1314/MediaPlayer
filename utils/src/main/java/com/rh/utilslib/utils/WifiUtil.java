package com.rh.utilslib.utils;

import android.Manifest;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.support.annotation.RequiresPermission;
import android.text.TextUtils;

import java.util.List;

import static android.content.Context.WIFI_SERVICE;

/**
 * @author yang yingwei
 * @description
 * @date 2018/3/15
 * @modify
 */

public class WifiUtil {

    private static WifiUtil sWifiUtils;
    private WifiManager mWm;
    private Context mContext;


    private WifiUtil(Context context) {
        this.mContext = context;
        mWm = (WifiManager) mContext.getApplicationContext().getSystemService(WIFI_SERVICE);
    }

    public static WifiUtil getInstance(Context context) {
        if (sWifiUtils == null) {
            sWifiUtils = new WifiUtil(context);
        }
        return sWifiUtils;
    }

    @RequiresPermission(allOf = {Manifest.permission.ACCESS_WIFI_STATE,Manifest.permission.CHANGE_WIFI_STATE})
    public int getCurrentWifiLevel(){
        startScan();
        String curSSID = getCurrentWifiSSID();
        List<ScanResult> scans = mWm.getScanResults();
        if(scans != null){
            for(ScanResult sr : scans){
                if(!TextUtils.isEmpty(sr.SSID) && !curSSID.equals(sr.SSID)){
                    int l = mWm.calculateSignalLevel(sr.level, 8)+1;
                    switch (l){
                        case 0:
                            return 1;
                        case 1:
                        case 2:
                        case 3:
                            return 2;
                        case 4:
                        case 5:
                        case 6:
                            return 3;
                        case 7:
                        case 8:
                        default:
                            return 4;
                    }
                }
            }
        }
        return 0;
    }

    @RequiresPermission(allOf = {Manifest.permission.ACCESS_WIFI_STATE})
    public String getCurrentWifiSSID() {
        if (mWm != null) {
            String ssid = mWm.getConnectionInfo().getSSID();
            return ssid.substring(1,ssid.length()-1);
        }
        return "";
    }

    @RequiresPermission(allOf = {Manifest.permission.CHANGE_WIFI_STATE})
    public boolean startScan() {
        return mWm.startScan();
    }

    @RequiresPermission(allOf = {Manifest.permission.CHANGE_WIFI_STATE})
    public void setWifiEnabled(boolean enable){
        if(mWm != null){
            mWm.setWifiEnabled(enable);
        }
    }

    public boolean isWifiEnabled(){
        if(mWm != null) {
            return mWm.isWifiEnabled();
        }
        return false;
    }


}
