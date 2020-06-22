package com.rh.utilslib.utils;

import java.util.Calendar;
import java.util.Date;

/**
 * @author yang yingwei
 * @description
 * @date 2018/1/11
 * @modify
 */

public class TimeUtil {

    public static long currentTime(){
        return System.currentTimeMillis()/1000;
    }

    public static void delay(long millis){
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


        private static final java.text.DateFormat DEFAULT_FORMAT = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault());

        /**
     * 将时间戳转为时间字符串
     * <p>格式为 yyyy-MM-dd HH:mm:ss</p>
     *
     * @param millis 毫秒时间戳
     * @return 时间字符串
     */
    public static String millis2String(final long millis) {
        return millis2String(millis, DEFAULT_FORMAT);
    }

        /**
     * 将时间戳转为时间字符串
     * <p>格式为 format</p>
     *
     * @param millis 毫秒时间戳
     * @param format 时间格式
     * @return 时间字符串
     */
    public static String millis2String(final long millis, final java.text.DateFormat format) {
        return format.format(new java.util.Date(millis));
    }


    public static long getTodayZoreTime(){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTime().getTime()/1000;
    }


}
