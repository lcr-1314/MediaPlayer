package com.rh.utilslib.utils;

import android.os.Environment;
import android.text.TextUtils;

import java.io.File;

/**
 * @author yang yingwei
 * @description
 * @date 2018/1/23
 * @modify
 */

public class SdcardUtil {

    /***
     * 是否有Sdcard
     * @return
     */
    public static boolean hasExternalStorage() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    /***
     * 剩余空间百分比
     * @return
     */
    public static float freeSpacePercent() {
        long freeSpace;
        long totalSpace;
        if (hasExternalStorage()) {
            freeSpace = Environment.getExternalStorageDirectory().getFreeSpace();
            totalSpace = Environment.getExternalStorageDirectory().getTotalSpace();
        } else {
            freeSpace = Environment.getDataDirectory().getFreeSpace();
            totalSpace = Environment.getDataDirectory().getTotalSpace();
        }

        return freeSpace * 100 / (float)totalSpace;
    }

    /***
     * 文件夹占总空间的大小
     * @param path
     * @return
     */
    public static float folderSizePercent(String path, boolean log) {
        if (TextUtils.isEmpty(path) || !(new File(path).exists())) {
            return 0;
        }

        long folderSize = getFolderSize(new File(path));
        long totalSpace;
        if (hasExternalStorage()) {
            totalSpace = Environment.getExternalStorageDirectory().getTotalSpace();
        } else {
            totalSpace = Environment.getDataDirectory().getTotalSpace();
        }
        if(log){
            LogUtil.d("test","folderSizePercent:"+(folderSize * 100 / (float)totalSpace) + "%   " +
                    "folderSize:" + ((float)folderSize/1024/1024) + "MB   " +
                    "folderSize:"+folderSize+"KB");
        }
        return (folderSize * 100 / (float)totalSpace);
    }

    /***
     * 文件夹的大小
     * @param file
     * @return
     */
    public static long getFolderSize(File file) {
        long size = 0;
        try {
            java.io.File[] fileList = file.listFiles();
            for (int i = 0; i < fileList.length; i++) {
                if (fileList[i].isDirectory()) {
                    size = size + getFolderSize(fileList[i]);
                } else {
                    size = size + fileList[i].length();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //return size/1048576;
        return size;
    }


}
