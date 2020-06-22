package com.rh.utilslib.utils;

import android.Manifest;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.annotation.RequiresPermission;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.rh.utilslib.UtilsLib;

/**
 * @author yang yingwei
 * @description
 * @date 2018/5/24
 * @modify
 */

public class SimCardUtil {

    private static String TAG = "SimCardUtil";

    private TelephonyManager telephonyManager;
    //移动运营商编号
    private String NetworkOperator;

    public SimCardUtil() {
        telephonyManager = (TelephonyManager) UtilsLib.getInstance().getSystemService(Context.TELEPHONY_SERVICE);
    }

    /***
     * 获取sim卡iccid
     * @return
     */
    @RequiresPermission(allOf = {Manifest.permission.READ_PHONE_STATE})
    public String getIccid() {
        String iccid = "N/A";
        iccid = telephonyManager.getSimSerialNumber();
        return iccid;
    }

    /***
     * 获取电话号码
     * @return
     */
    @RequiresPermission(allOf = {Manifest.permission.READ_PHONE_STATE})
    public String getNativePhoneNumber() {
        String nativePhoneNumber = "N/A";
        nativePhoneNumber = telephonyManager.getLine1Number();
        return nativePhoneNumber;
    }

    /***
     * 获取手机服务商信息
     * @return
     */
    public String getProvidersName() {
        String providersName = "N/A";
        NetworkOperator = telephonyManager.getNetworkOperator();
        //IMSI号前面3位460是国家，紧接着后面2位00 02是中国移动，01是中国联通，03是中国电信。
        if (NetworkOperator.equals("46000") || NetworkOperator.equals("46002")) {
            providersName = "中国移动";//中国移动
        } else if(NetworkOperator.equals("46001")) {
            providersName = "中国联通";//中国联通
        } else if (NetworkOperator.equals("46003")) {
            providersName = "中国电信";//中国电信
        }
        return providersName;

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @RequiresPermission(allOf = {Manifest.permission.READ_PHONE_STATE})
    public String getPhoneInfo() {
        TelephonyManager tm = (TelephonyManager) UtilsLib.getInstance().getSystemService(Context.TELEPHONY_SERVICE);
        StringBuffer sb = new StringBuffer();

        sb.append("\nLine1Number = " + tm.getLine1Number());
        sb.append("\ngetGroupIdLevel1 = " + tm.getGroupIdLevel1());
        sb.append("\nNetworkOperator = " + tm.getNetworkOperator());//移动运营商编号
        sb.append("\nNetworkOperatorName = " + tm.getNetworkOperatorName());//移动运营商名称
        sb.append("\nSimCountryIso = " + tm.getSimCountryIso());
        sb.append("\nSimOperator = " + tm.getSimOperator());
        sb.append("\nSimOperatorName = " + tm.getSimOperatorName());
        sb.append("\nSimSerialNumber = " + tm.getSimSerialNumber());
        sb.append("\nSubscriberId(IMSI) = " + tm.getSubscriberId());
        return  sb.toString();
    }


    @RequiresPermission(allOf = {Manifest.permission.READ_PHONE_STATE})
    public String getImsi(){
        try {
            String imsi = telephonyManager.getSubscriberId();
            return TextUtils.isEmpty(imsi) ? "" : imsi;
        }catch (Throwable t){
            t.printStackTrace();
            return "";
        }
    }
}
