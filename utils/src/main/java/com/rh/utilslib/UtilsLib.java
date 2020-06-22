package com.rh.utilslib;

import android.content.Context;

import com.rh.utilslib.utils.LogUtil;

/**
 * @author yang yingwei
 * @description
 * @date 2018/1/3
 * @modify
 */

public class UtilsLib {

    private static Context mContext;
    public static boolean isLinphoneLog = false;


    public static void init(Context context,boolean isDebug){
        mContext = context;
        LogUtil.setDebug(isDebug);
    }

    public static Context getInstance(){
        if(mContext == null){
            throw new RuntimeException("工具库未初始化");
        }
        return mContext;
    }

    public static void setLinphoneLog(boolean debug){
        isLinphoneLog = debug;
    }

}
