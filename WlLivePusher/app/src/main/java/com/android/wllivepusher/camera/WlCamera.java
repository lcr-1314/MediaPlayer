package com.android.wllivepusher.camera;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;

import com.android.wllivepusher.util.DisplayUtil;

import java.io.IOException;
import java.util.List;

public class WlCamera {
    private Camera camera;

    private SurfaceTexture surfaceTexture;
    private int width;
    private int height;

    public WlCamera(Context context) {
        // 获取屏幕长宽
        this.width = DisplayUtil.getScreenWidth(context);
        this.height = DisplayUtil.getScreenHeight(context);
    }

    // 将surfaceTexture绑定到Camera上，打开摄像头预览
    public void initCamera(SurfaceTexture surfaceTexture, int cameraId) {
        this.surfaceTexture = surfaceTexture; // surface
        setCameraParm(cameraId);
    }

    private void setCameraParm(int cameraId) {
        try {
            camera = Camera.open(cameraId);

            camera.setPreviewTexture(surfaceTexture);       // 将camera绑定到surfaceTexture上
            Camera.Parameters parameters = camera.getParameters();

            parameters.setFlashMode("on"); // 关闭闪光灯
            parameters.setPreviewFormat(ImageFormat.NV21); // 视频格式

            Camera.Size size = getFitSize(parameters.getSupportedPictureSizes());
            parameters.setPictureSize(size.width, size.height);

            size = getFitSize(parameters.getSupportedPreviewSizes());
            parameters.setPreviewSize(size.width, size.height);

            // 自动对焦
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);

            camera.setParameters(parameters);   // 设置参数
            camera.startPreview();  //开始预览
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopPreview() {
        if (camera != null) {
            camera.startPreview();
            camera.release();
            camera = null;
        }
    }

    // 切换前后摄像头
    public void changeCamera(int cameraId) {
        if (camera != null) {
            stopPreview();
        }
        setCameraParm(cameraId);
    }

    private Camera.Size getFitSize(List<Camera.Size> sizes) {
        if (width < height) {
            int t = height;
            height = width;
            width = t;
        }

        for (Camera.Size size : sizes) {
            if (1.0f * size.width / size.height == 1.0f * width / height) {
                return size;
            }
        }
        return sizes.get(0);
    }
}
