package com.rh.utilslib.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.renderscript.Allocation;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;

/**
 *@author  Bill xiang
 *@date 2018/1/16
 *@note 使用Base64 Bitmap 处理类
 **/
public class BitmapUtils {


    //拍照图片保存路径
    public static final String PHOTO_PATH = "mnt/sdcard/CAMERA_DEMO/Camera/";

    public static final String UUID = Build.SERIAL.toLowerCase();

    /**
     * 空间压缩
     *
     * @param bitmap  原图
     * @param maxSize 最大尺寸 kb
     * @return
     */
    public static Map<String, Object> compressSpace(Bitmap bitmap, int maxSize,
                                                    int step, boolean output) {
        Map<String, Object> result = new HashMap<>();
        int quality = 100;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
        // 循环判断如果压缩后图片是否大于100kb,大于继续压缩
        while (baos.toByteArray().length / 1024 > maxSize) {
            baos.reset();
            // 每次都减少步长
            quality -= (step >= quality ? 0 : step);
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
            break;
        }
        Bitmap streamBitmap = BitmapFactory.decodeStream(
                new ByteArrayInputStream(baos.toByteArray()), null, null);
        RandomAccessFile raf = null;
        byte[] array = null;
        try {
            array = baos.toByteArray();
            if (output) {
                File imageFile =  new File(PHOTO_PATH,UUID);
                if (null != imageFile) {
                    raf = new RandomAccessFile(imageFile, "rw");
                    raf.write(array);
                    raf.close();
                    result.put("file", imageFile);
                }
            }
            baos.flush();
            baos.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (raf != null) raf.close();
                baos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        result.put("array", array);
        result.put("bitmap", streamBitmap);
        return result;
    }

    /**
     * 保存位图到磁盘
     *
     * @param filePath 保存路径
     * @param bitmap   位图
     * @param format   文件压缩格式
     * @return
     */
    public static boolean save2Disk(@NonNull String filePath, Bitmap bitmap,
                                    @NonNull Bitmap.CompressFormat format) {
        if (FileUtil.prepareDirs(FileUtil.getParentDir(filePath))) {
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(filePath);
                return bitmap.compress(format, 100, fos);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (null != fos) fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return false;
    }

    /**
     * Android SDK RenderScript 高斯模糊
     * note: sdk > 17 系统直接支持，如果想兼容以前版本必须导入
     * $ANDROID_SDK/build-tools/renderscript/lib/RenderScript-v8.jar
     * 和 $ANDROID_SDK/build-tools/renderscript/lib/packaged/动态库(必须导入librsjni.so

     和libRSSupport.so

     )
     *
     * @param context 上下文
     * @param src     原图
     * @param radius  模糊半径 0 - 100
     * @return 模糊结果
     */
    @TargetApi(18)
    public static Bitmap scriptBlur(@NonNull Context context, @NonNull Bitmap src,
                                    @FloatRange(from = 1, to = 100) float radius, Bitmap dst) {
        if (null == src || 0 >= radius || 100 < radius) {
            return src;
        }

        src = buildBlurBitmap(src, Bitmap.Config.ARGB_8888, radius);
        if (null == dst || dst.isRecycled()) {
            dst = src.copy(Bitmap.Config.ARGB_8888, true);
        }
        dst.eraseColor(Color.TRANSPARENT);
        RenderScript rs = RenderScript.create(context);
        Allocation input = Allocation.createFromBitmap(rs, src);
        Allocation output = Allocation.createTyped(rs, input.getType());
        ScriptIntrinsicBlur blur = ScriptIntrinsicBlur.create(rs, output.getElement());

        blur.setInput(input);
        int loop = ((int) radius) / 25;
        int remain = ((int) radius) % 25;
        for (int c = 0; c < loop; c++) {
            blur.setRadius(25);
            blur.forEach(output);
        }
        if (remain > 0) {
            blur.setRadius(remain);
            blur.forEach(output);
        }

        output.copyTo(dst);
        rs.destroy();
        return dst;
    }

    /**
     * 构建模糊结果输出位图
     * @param src       原位图
     * @param radius    模糊半径
     */
    private static Bitmap buildBlurBitmap(Bitmap src, @Nullable Bitmap.Config config, float radius) {
        if (null == src || src.isRecycled() || radius < 1) {
            return null;
        }

        float scale = Math.max(src.getWidth(), src.getHeight()) / 200f;

        if (null != config && config != src.getConfig()) {
            return Bitmap.createScaledBitmap(src,
                    Math.round(src.getWidth() / scale), Math.round(src.getHeight() / scale),
                    false).copy(config, true);
        } else {
            return Bitmap.createScaledBitmap(src,
                    Math.round(src.getWidth() / scale), Math.round(src.getHeight() / scale),
                    false);
        }
    }
}
