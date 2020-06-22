package com.rh.utilslib.utils;

import android.graphics.Bitmap;
import android.graphics.Matrix;

/**
 * @author yang yingwei
 * @description
 * @date 2018/7/19
 * @modify
 */

public class ImageUtil {

    /**
     * 图片反转
     *
     * @param bmp
     * @param flag 0为水平反转，1为垂直反转
     * @return
     */
    public static Bitmap reverseBitmap(Bitmap bmp, int flag) {

        float[] floats = null;
        switch (flag) {
            case 0: // 水平反转
                floats = new float[]{-1f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f};
                break;
            case 1: // 垂直反转
                floats = new float[]{1f, 0f, 0f, 0f, -1f, 0f, 0f, 0f, 1f};
                break;
        }

        if (floats != null) {
            Matrix matrix = new Matrix();
            matrix.setValues(floats);
            return Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
        } else {
            return bmp;
        }

    }


    public static Bitmap rotateBitmap(Bitmap bmp, int degrees) {

        if (degrees != 0) {
            Matrix matrix = new Matrix();
            matrix.setRotate(degrees);
            return Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
        } else {
            return bmp;
        }
    }
}
