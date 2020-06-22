package com.rh.utilslib.utils;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import com.rh.utilslib.UtilsLib;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author yang yingwei
 * @description 文件操作工具类
 * @date 2017/12/28
 * @modify
 */

public class FileUtil {

    /***
     * 删除指定路径的文件或文件夹
     * @param path
     */
    public static synchronized void deleteFile(String path){
        File file = new File(path);
        if(file.exists()){
            deleteFile(file);
        }
    }

    /***
     * 删除指定的文件或文件夹
     * @param file
     */
    public static synchronized void deleteFile(File file){
        if(file==null || !file.exists()){
            return;
        }
        if(file.isFile()){//删除指定的文件
            file.delete();
        }else {//删除整个目录中的内容
            File[] files = file.listFiles();
            if(files != null && files.length>0){
                for(File f : files){
                    deleteFile(f);
                }
            }
            file.delete();
        }
    }

    public static String getFileName(String path){
        if(TextUtils.isEmpty(path)){
            return "";
        }

        if(isFilesExist(path)){
            return new File(path).getName();
        }
        return "";
    }


    public static String getPhotoName() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat("'RH'_yyyyMMdd_HHmmss");
        return dateFormat.format(date) + ".jpg";
    }

    /**
     * 获取父路径
     *
     * @param path 路径
     * @return
     */
    public static String getParentDir(String path) {
        return null == path ? null : new File(path).getParent();
    }

    /**
     * 创建目录
     *
     * @param paths 目录s
     * @return 全部创建成功返回true
     */
    public static boolean prepareDirs(String... paths) {
        for (String path : paths) {
            if (!isFilesExist(path)) {
                if (TextUtils.isEmpty(path) || !new File(path).mkdirs()) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * 判定 路径列表是否全部存在
     *
     * @param paths 路径列表
     * @return true, false
     */
    public static boolean isFilesExist(String... paths) {
        if (null != paths) {
            for (String path : paths) {
                if (!TextUtils.isEmpty(path)) {
                    File file = new File(path);
                    if (!file.exists()) {
                        return false;
                    }
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 判断字符串是否为null或全为空格
     *
     * @param s 待校验字符串
     * @return {@code true}: null或全空格<br> {@code false}: 不为null且不全空格
     */
    public static boolean isSpace(String s) {
        return (s == null || s.trim().length() == 0);
    }

//    public static boolean openFile(Context context, String appMd5, boolean isUpdateNow,String DOWNLOADPATH) {
//        if (isUpdateNow) {
//            return openFile(context, appMd5,DOWNLOADPATH);
//        } else {
//            Intent alarmIntent = new Intent(context, UpDateAlarmService.class);
//            context.startService(alarmIntent);
//            return false;
//        }
//    }

    public static boolean openFile(String appMd5,String DOWNLOADPATH) {
        boolean upDateState = false;
        String appPath = Environment.getExternalStorageDirectory().getAbsolutePath() + DOWNLOADPATH + "app.apk";
        String fileMd5 = EncryptUtil.encryptMD5File2String(appPath);
        if (TextUtils.isEmpty(appMd5) || TextUtils.isEmpty(fileMd5)) {
            return false;
        }
        File file = new File(appPath);
        if (TextUtils.equals(appMd5.toLowerCase(), fileMd5.toLowerCase())) {
            Intent intent = new Intent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setAction(Intent.ACTION_VIEW);
            String type = getMIMEType(file);
            intent.setDataAndType(Uri.fromFile(file), type);
            UtilsLib.getInstance().startActivity(intent);
            upDateState = true;
        } else {
            if (file.exists()) {
                file.delete();
            }
            upDateState = false;
        }
        return upDateState;
    }

    public static String getMIMEType(File var0) {
        String var1 = "";
        String var2 = var0.getName();
        String var3 = var2.substring(var2.lastIndexOf(".") + 1, var2.length()).toLowerCase();
        var1 = MimeTypeMap.getSingleton().getMimeTypeFromExtension(var3);
        return var1;
    }

    public static synchronized void appendContent(String path,String name,String content){
        try {
            File dir = new File(path);
            if(!dir.exists()){
                dir.mkdirs();
            }
            File file = new File(path,name);
            if(!file.exists()){
                file.createNewFile();
            }

            FileWriter fileWriter = new FileWriter(file,true);
            fileWriter.append(content);
            fileWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

    }

    public static String getFileContent(File file){
        if(!file.isFile()){
            return "";
        }
        StringBuilder sb = new StringBuilder();
        try {
            String tempStr;
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            while ((tempStr = bufferedReader.readLine()) != null){
                sb.append(tempStr).append("\n");
            }
            bufferedReader.close();
            fileReader.close();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
        return sb.toString();
    }

    public static void printFileContent(String tag, File file){
        if(!file.isFile()){
            return ;
        }

        StringBuilder sb = new StringBuilder();
        try {
            String tempStr;
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            while ((tempStr = bufferedReader.readLine()) != null){
                sb.append(tempStr).append("\n");
            }
            bufferedReader.close();
            fileReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (tag == null || tag.length() == 0 || sb == null || sb.length() == 0)
            return;

        String msg = sb.toString();

        int segmentSize = 3 * 1024;
        long length = msg.length();

        LogUtil.d(tag,"--------------------- " + tag + " " + file.getPath() + " start ---------------------");

        if (length <= segmentSize ) {// 长度小于等于限制直接打印
            LogUtil.d(tag, msg);
        }else {
            while (msg.length() > segmentSize ) {// 循环分段打印日志
                String logContent = msg.substring(0, segmentSize );
                msg = msg.replace(logContent, "");
                LogUtil.d(tag, logContent);
            }
            LogUtil.d(tag, msg);// 打印剩余日志
        }
        LogUtil.d(tag,"--------------------- " + tag + " " + file.getPath() + " end ---------------------");
    }


    public static void clearFile(File file){
        if(file == null || !file.isFile()){
            return ;
        }

        try {
            FileWriter fw = new FileWriter(file);
            fw.write("");
            fw.close();
        } catch (Throwable t){
            t.printStackTrace();
        }

    }


}
