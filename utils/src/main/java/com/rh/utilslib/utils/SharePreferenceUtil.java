package com.rh.utilslib.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.rh.utilslib.UtilsLib;

/**
 * @author Bill xiang
 * @description 本地SharePreference管理類
 * @date 2018/1/4
 * @modify
 */

public class SharePreferenceUtil {

    private static SharePreferenceUtil instance;
    private SharedPreferences sharedPreferences;
    private final String name = "share_data_v2";// 配置文件名
    private static final Object lock = new Object();
    private final int mode = Context.MODE_PRIVATE;// 读取模式，该为私有模式，只有本项目才能访问

    interface SharePreferenceInterface {
        String MQTT_IP = "mqtt_ip";
    }

    /**
     * 初始化文件名和读取模式
     */
    private SharePreferenceUtil() {
        sharedPreferences = UtilsLib.getInstance().getSharedPreferences(name, mode);
    }

    /**
     * 初始化工具
     *
     * @return
     */
    public static SharePreferenceUtil getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (null == instance) {
                    instance = new SharePreferenceUtil();
                }
            }
        }
        return instance;
    }


    /**
     * 获取音视频音量
     */
    public String getMqttIp() {
        return sharedPreferences.getString(SharePreferenceInterface.MQTT_IP, "");
    }


}
