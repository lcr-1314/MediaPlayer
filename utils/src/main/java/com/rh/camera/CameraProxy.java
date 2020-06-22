package com.rh.camera;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.util.Log;

import com.rh.utilslib.UtilsLib;
import com.rh.utilslib.utils.LogUtil;
import com.rh.utilslib.utils.ThreadManagerUtil;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author wilson
 * @description 拍照管理类
 * @date 2018/7/17
 * @modify
 */
final public class CameraProxy {

    public static String TAG = "CameraProxy";

    private static CameraProxy sManager;

    private static volatile Camera mFontCamera;
    private static volatile Camera mBackCamera;

    private Camera.PreviewCallback mCameraPreviewCallback;
    private Camera.PreviewCallback mCameraPreviewCallbackFont;
    private ZgCameraPreviewCallback mZgCameraPreviewCallback;
    private ZgCameraPreviewCallback mZgCameraPreviewCallbackFont;
    private Set<ZgCameraPreviewCallback> mZgBackCameraPreviewCallbackSet = new HashSet<>();
    private Set<ZgCameraPreviewCallback> mZgFontCameraPreviewCallbackSet = new HashSet<>();

    private int mExpectedFps = 30;
    private int mRotationBack = 0;
    private int mRotationTemp = 0;
    private int mFormat;
    private int mWidth = 640;
    private int mHeight = 480;
    private volatile int mPreviewWidth;
    private volatile int mPreviewHeight;

    private int mRotationFont = 0;
    private int mFormatFont;
    private int mWidthFont = 640;
    private int mHeightFont = 480;
    private volatile int mPreviewWidthFont;
    private volatile int mPreviewHeightFont;

    private Camera.Size mSizeFont;
    private Camera.Size mSizeBack;

    private boolean mOpenAndInit = false;
    private boolean mOpenAndInitFont = false;

    private volatile long mFontTimeTag = 0;
    private volatile long mBackTimeTag = 0;

    private volatile TakePictureCallback mPhotoDataCallback;

    private int mBufferSize = (mPreviewWidth * mPreviewHeight * mFormat) / 8;
    private byte[] mBackCameraBuffer = new byte[mBufferSize += mBufferSize / 20];
    private byte[] mFontCameraBuffer = new byte[mBufferSize += mBufferSize / 20];

    private SurfaceTexture mBackSurfaceTexture = new SurfaceTexture(10);
    private SurfaceTexture mFontSurfaceTexture = new SurfaceTexture(20);


    private CameraProxy() {

    }


    public static CameraProxy getInstance() {
        if (sManager == null) {
            synchronized (CameraProxy.class) {
                if (sManager == null) {
                    sManager = new CameraProxy();
                }
            }
        }
        return sManager;
    }

    public Camera.Size getSizeFont() {
        return mSizeFont;
    }

    public Camera.Size getSizeBack() {
        return mSizeBack;
    }

    public int getFormat() {
        return mFormat;
    }


    public void setFormat(int format) {
        this.mFormat = format;
    }


    public int getWidth() {
        return mWidth;
    }


    public void setWidth(int width) {
        this.mWidth = width;
    }


    public int getHeight() {
        return mHeight;
    }


    public void setHeight(int height) {
        this.mHeight = height;
    }


    public int getPreviewWidth() {
        return mPreviewWidth;
    }


    public int getPreviewHeight() {
        return mPreviewHeight;
    }


    public int getFormatFont() {
        return mFormatFont;
    }

    public int getPreviewWidthFont() {
        return mPreviewWidthFont;
    }


    public int getPreviewHeightFont() {
        return mPreviewHeightFont;
    }

    public void getPhotoData(TakePictureCallback pd) {
        mPhotoDataCallback = pd;
    }


    public boolean isOpenAndInit() {
        return mOpenAndInit && (System.currentTimeMillis() - mBackTimeTag) < 1000;
    }


    public boolean isOpenAndInitFont() {
        return mOpenAndInitFont && (System.currentTimeMillis() - mFontTimeTag) < 1000;
    }

    public int getmWidthFont() {
        return mWidthFont;
    }

    public void setmWidthFont(int mWidthFont) {
        this.mWidthFont = mWidthFont;
    }
    public void setRotationTemp(int mRotationTemp) {
        this.mRotationTemp = mRotationTemp;
    }

    public int getmHeightFont() {
        return mHeightFont;
    }

    public void setmHeightFont(int mHeightFont) {
        this.mHeightFont = mHeightFont;
    }

    public int getRotationBack() {
        return mRotationBack;
    }

    public int getRotationTemp() {
        return mRotationTemp;
    }


    public int getRotationFont() {
        return mRotationFont;
    }


    /***
     *
     * @param cameraId
     * @return
     */
    final public Camera getCamera(int cameraId) {
        if (Camera.CameraInfo.CAMERA_FACING_FRONT == cameraId) {
            if (mFontCamera == null || System.currentTimeMillis() - mFontTimeTag > 1000) {
                releaseCamera(cameraId);
                openAndInitFont(cameraId);
            }
            return mFontCamera;
        } else if (Camera.CameraInfo.CAMERA_FACING_BACK == cameraId) {
            if (mBackCamera == null || System.currentTimeMillis() - mBackTimeTag > 1000) {
                releaseCamera(cameraId);
                openAndInit();
            }
            return mBackCamera;
        }
        return null;
    }

    public Camera getCamera() {
        return backCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
    }

    /***
     * 在子线程中打开和初始化相机
     */
    public void openAndInit() {
        ThreadManagerUtil.start(new Runnable() {
            @Override
            public void run() {
                backCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
            }
        });
    }

    /***
     * 在子线程中打开和初始化相机
     */
    public void openAndInitFont(final int cameraId) {
        ThreadManagerUtil.start(new Runnable() {
            @Override
            public void run() {
                if (Camera.CameraInfo.CAMERA_FACING_FRONT == cameraId) {
                    fontCamera(cameraId);
                } else if (Camera.CameraInfo.CAMERA_FACING_BACK == cameraId) {
                    backCamera(cameraId);
                }
            }
        });

    }

    /***
     *
     * @return
     */
    private Camera fontCamera(int tagInfo) {
        try {
            if (mFontCamera != null) {

            }
            if (mFontCamera == null) {
                initCameraRotation(tagInfo);
                mFontCamera = Camera.open(tagInfo);
                ///////////////////////////////////////////////////////////////////////
                Camera.Parameters params = mFontCamera.getParameters();
                params.setPreviewSize(mWidthFont, mHeightFont);
                int[] chosenFps = findClosestEnclosingFpsRange(mExpectedFps * 1000, params.getSupportedPreviewFpsRange());
                params.setPreviewFpsRange(chosenFps[0], chosenFps[1]);
                mFontCamera.setParameters(params);
                //applyCameraParameters(mFontCamera, width, height, fps);

                Camera.Parameters newParams = mFontCamera.getParameters();
                Camera.Size previewSize = newParams.getPreviewSize();
                mSizeFont = previewSize;
                mPreviewWidthFont = previewSize.width;
                mPreviewHeightFont = previewSize.height;
                mFormatFont = mFontCamera.getParameters().getPreviewFormat();
//                LogUtil.e(TAG, "setCameraDisplayOrientation:  " +info.facing+"   degrees: "+result+" info.orientation: "+info.orientation);

                int bufferSize = (mPreviewWidthFont * mPreviewHeightFont * ImageFormat.getBitsPerPixel(mFormatFont)) / 8;
                mFontCamera.addCallbackBuffer(new byte[bufferSize]);
                mFontCamera.addCallbackBuffer(new byte[bufferSize]);
                mFontCamera.addCallbackBuffer(new byte[bufferSize]);

                ///////////////////////////////////////////////////////////////////////
                mFontCamera.setPreviewTexture(mFontSurfaceTexture);
                mFontCamera.setPreviewCallbackWithBuffer(getCameraPreviewCallbackFont());
                setCameraDisplayOrientation(mRotationFont, tagInfo, mFontCamera);

                mFontCamera.startPreview();
                mOpenAndInitFont = true;
                LogUtil.d(TAG, "FontCamera 初始化成功");
            }
        } catch (Throwable t) {
            mOpenAndInitFont = false;
            LogUtil.d(TAG, "FontCamera 初始化失败");
            t.printStackTrace();
        }
        return mFontCamera;
    }

    /***
     *
     * @return
     */
    private Camera backCamera(int cameraId) {
        try {
            if (mBackCamera != null) {

            }
            if (mBackCamera == null) {
                initCameraRotation(cameraId);
                mBackCamera = Camera.open(cameraId);
                ///////////////////////////////////////////////////////////////////////
                Camera.Parameters params = mBackCamera.getParameters();
                params.setPreviewSize(mWidth, mHeight);
                int[] chosenFps = findClosestEnclosingFpsRange(mExpectedFps * 1000, params.getSupportedPreviewFpsRange());
                params.setPreviewFpsRange(chosenFps[0], chosenFps[1]);
                mBackCamera.setParameters(params);
                //applyCameraParameters(mFontCamera, width, height, fps);

                Camera.Parameters newParams = mBackCamera.getParameters();
                Camera.Size previewSize = newParams.getPreviewSize();
                mSizeBack = previewSize;
                mPreviewWidth = previewSize.width;
                mPreviewHeight = previewSize.height;
                mFormat = mBackCamera.getParameters().getPreviewFormat();

                int bufferSize = (mPreviewWidth * mPreviewHeight * ImageFormat.getBitsPerPixel(mFormat)) / 8;
                mBackCamera.addCallbackBuffer(new byte[bufferSize]);
                mBackCamera.addCallbackBuffer(new byte[bufferSize]);
                mBackCamera.addCallbackBuffer(new byte[bufferSize]);

                ///////////////////////////////////////////////////////////////////////
                mBackCamera.setPreviewTexture(mBackSurfaceTexture);
                mBackCamera.setPreviewCallbackWithBuffer(getCameraPreviewCallback());
                setCameraDisplayOrientation(mRotationBack, cameraId, mBackCamera);

                mBackCamera.startPreview();
                mOpenAndInit = true;
                LogUtil.d(TAG, "BackCamera 初始化成功");
            }
        } catch (Throwable t) {
            mOpenAndInit = false;
            LogUtil.d(TAG, "BackCamera 初始化失败");
            t.printStackTrace();
        }
        return mBackCamera;
    }

    private void initCameraRotation(int currentCameraId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CameraManager manager = (CameraManager) UtilsLib.getInstance().getSystemService(Context.CAMERA_SERVICE);
            String[] cameraList;
            try {
                cameraList = manager.getCameraIdList();
                for (int i = 0; i < cameraList.length; i++) {
                    String cameraId = cameraList[i];
                    CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
                    int camOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
                    if (currentCameraId == CameraCharacteristics.LENS_FACING_FRONT) {
                        mRotationFont = camOrientation;
                        break;
                    } else {
                        mRotationBack = camOrientation;
                        break;
                    }
                }
                    LogUtil.d("linPhone", "mRotationFont    " + mRotationFont + "   mRotationBack  " + mRotationBack);

            } catch (Throwable t) {
                t.printStackTrace();
            }
        } else {
            for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
                Camera.CameraInfo info = new Camera.CameraInfo();
                Camera.getCameraInfo(i, info);
                if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    mRotationFont = info.orientation;
                    break;
                } else {
                    mRotationBack = info.orientation;
                    break;
                }
            }
        }
    }

    /***
     *
     * @param cameraId
     */
    public void releaseCamera(int cameraId) {
        if (Camera.CameraInfo.CAMERA_FACING_FRONT == cameraId) {
            if (mFontCamera != null) {
                mFontCamera.setPreviewCallback(null);
                mFontCamera.stopPreview();
                mFontCamera.release();
                mFontCamera = null;
                mOpenAndInitFont = false;
            }
        } else if (Camera.CameraInfo.CAMERA_FACING_BACK == cameraId) {
            if (mBackCamera != null) {
                mBackCamera.setPreviewCallback(null);
                mBackCamera.stopPreview();
                mBackCamera.release();
                mBackCamera = null;
                mOpenAndInit = false;
            }
        }
    }


    /**
     * @return 前置摄像头的ID
     */
    public int getFrontCameraId() {
        return getCameraId(Camera.CameraInfo.CAMERA_FACING_FRONT);
    }

    /**
     * @return 后置摄像头的ID
     */
    public int getBackCameraId() {
        return getCameraId(Camera.CameraInfo.CAMERA_FACING_BACK);
    }

    /**
     * @param tagInfo
     * @return 得到特定camera info的id
     */
    private int getCameraId(int tagInfo) {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        // 开始遍历摄像头，得到camera info
        int cameraId, cameraCount;
        for (cameraId = 0, cameraCount = Camera.getNumberOfCameras(); cameraId < cameraCount; cameraId++) {
            Camera.getCameraInfo(cameraId, cameraInfo);

            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT && cameraInfo.facing == tagInfo) {
                return Camera.CameraInfo.CAMERA_FACING_FRONT;
            } else if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK && cameraInfo.facing == tagInfo) {
                return Camera.CameraInfo.CAMERA_FACING_BACK;
            }
        }
        return cameraId;
    }


    public void setZgCameraPreviewCallback(ZgCameraPreviewCallback cb) {
        this.mZgCameraPreviewCallback = cb;
    }

    public void addZgCameraPreviewCallback(ZgCameraPreviewCallback cb) {
        if(mZgBackCameraPreviewCallbackSet != null){
            mZgBackCameraPreviewCallbackSet.add(cb);
        }
    }

    public void removeZgCameraPreviewCallback(ZgCameraPreviewCallback cb){
        if(mZgBackCameraPreviewCallbackSet != null){
            mZgBackCameraPreviewCallbackSet.remove(cb);
        }
    }


    public void setZgCameraPreviewCallbackFont(ZgCameraPreviewCallback cb) {
        this.mZgCameraPreviewCallbackFont = cb;
    }


    public void addZgCameraPreviewCallbackFont(ZgCameraPreviewCallback cb) {
        if(mZgFontCameraPreviewCallbackSet != null){
            mZgFontCameraPreviewCallbackSet.add(cb);
        }
    }


    public void removeZgCameraPreviewCallbackFont(ZgCameraPreviewCallback cb){
        if(mZgFontCameraPreviewCallbackSet != null){
            mZgFontCameraPreviewCallbackSet.remove(cb);
        }
    }


    public interface ZgCameraPreviewCallback {
        void onPreviewFrame(byte[] data, Camera camera);
    }


    private Camera.PreviewCallback getCameraPreviewCallback() {
        if (mCameraPreviewCallback == null) {
            mCameraPreviewCallback = new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {
                    try {
                        mBackTimeTag = System.currentTimeMillis();
                        if (data == null) {
                            //Log.d(TAG, "Back camera onPreviewFrame data: null");
                            camera.addCallbackBuffer(mBackCameraBuffer);
                        } else {
                            //Log.d(TAG, "Back camera onPreviewFrame data:" + data.length);
                            camera.addCallbackBuffer(data);
                            camera.setPreviewTexture(mBackSurfaceTexture);
                            if (mPhotoDataCallback != null) {
                                mPhotoDataCallback.takePicture(data.clone(), mPreviewWidth, mPreviewHeight);
                                mPhotoDataCallback = null;
                            }
                            if (mZgCameraPreviewCallback != null) {
                                mZgCameraPreviewCallback.onPreviewFrame(data, camera);
                            }
                            if(mZgBackCameraPreviewCallbackSet != null){
                                for(ZgCameraPreviewCallback cb : mZgBackCameraPreviewCallbackSet){
                                    cb.onPreviewFrame(data, camera);
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            };
        }
        return mCameraPreviewCallback;
    }


    private Camera.PreviewCallback getCameraPreviewCallbackFont() {
        if (mCameraPreviewCallbackFont == null) {
            mCameraPreviewCallbackFont = new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {
                    try {
                        mFontTimeTag = System.currentTimeMillis();
                        if (data == null) {
                            Log.d(TAG, "Font camera onPreviewFrame() data: null");
                            camera.addCallbackBuffer(mFontCameraBuffer);
                        } else {
                            Log.d(TAG, "Font camera onPreviewFrame() data:" + data.length);
                            camera.addCallbackBuffer(data);
                            camera.setPreviewTexture(mFontSurfaceTexture);
                            if (mPhotoDataCallback != null) {
                                mPhotoDataCallback.takePicture(data.clone(), mPreviewWidthFont, mPreviewHeightFont);
                                mPhotoDataCallback = null;
                            }
                            if (mZgCameraPreviewCallbackFont != null) {
                                mZgCameraPreviewCallbackFont.onPreviewFrame(data, camera);
                            }
                            if(mZgFontCameraPreviewCallbackSet != null){
                                for(ZgCameraPreviewCallback cb : mZgFontCameraPreviewCallbackSet){
                                    cb.onPreviewFrame(data, camera);
                                }
                            }
                        }
                    } catch (IOException e){
                        e.printStackTrace();
                    }
                }
            };
        }
        return mCameraPreviewCallbackFont;
    }


    //////////////////////////////////////////////////////////////////////////////////////
    private static int[] findClosestEnclosingFpsRange(int expectedFps, List<int[]> fpsRanges) {
        if (fpsRanges == null || fpsRanges.size() == 0) {
            return new int[]{0, 0};
        }

        // init with first element
        int[] closestRange = fpsRanges.get(0);
        int measure = Math.abs(closestRange[0] - expectedFps)
                + Math.abs(closestRange[1] - expectedFps);
        for (int[] curRange : fpsRanges) {
            if (curRange[0] > expectedFps || curRange[1] < expectedFps) continue;
            int curMeasure = Math.abs(curRange[0] - expectedFps)
                    + Math.abs(curRange[1] - expectedFps);
            if (curMeasure < measure) {
                closestRange = curRange;
                measure = curMeasure;
            }
        }

        LogUtil.d(TAG, "fps ranges:" + closestRange[0] + "~" + closestRange[1]);
        return closestRange;
    }

    protected static void applyCameraParameters(Camera camera, int width, int height, int requestedFps) {
        Camera.Parameters params = camera.getParameters();

        params.setPreviewSize(width, height);

        List<Integer> supported = params.getSupportedPreviewFrameRates();
        if (supported != null) {
            int nearest = Integer.MAX_VALUE;
            for (Integer fr : supported) {
                int diff = Math.abs(fr.intValue() - requestedFps);
                if (diff < nearest) {
                    nearest = diff;
                    params.setPreviewFrameRate(fr.intValue());
                }
            }
            Log.d(TAG, "Preview frame rate set:" + params.getPreviewFrameRate());
        }

        camera.setParameters(params);
    }

    private static void setCameraDisplayOrientation(int rotationDegrees, int cameraId, Camera camera) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + rotationDegrees) % 360;
            result = (360 - result) % 360; // compensate the mirror
        } else { // back-facing
            result = (info.orientation - rotationDegrees + 360) % 360;
        }
        LogUtil.e(TAG, "setCameraDisplayOrientation:  " + info.facing + "   degrees: " + result + " info.orientation: " + info.orientation);

        try {
            camera.setDisplayOrientation(result);
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }

    public interface TakePictureCallback {
        void takePicture(byte[] data, int width, int height);
    }

    @Override
    protected void finalize() throws Throwable {
        LogUtil.d(TAG, "Camera release...");
        releaseCamera(Camera.CameraInfo.CAMERA_FACING_FRONT);
        releaseCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
        super.finalize();
    }
}
