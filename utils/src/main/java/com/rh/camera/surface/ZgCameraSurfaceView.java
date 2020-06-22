package com.rh.camera.surface;

import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.guo.android_extend.tools.FrameHelper;
import com.libyuv.util.YuvUtil;
import com.rh.camera.CameraProxy;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author yang yingwei
 * @description
 * @date 2018/7/17
 * @modify
 */

public class ZgCameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback, ZgCameraGLSurfaceView.OnRenderListener {
    private final String TAG = this.getClass().getSimpleName();

    private Camera mCamera;
    private int mWidth, mHeight, mFormat;
    private OnCameraListener mOnCameraListener;
    private FrameHelper mFrameHelper;
    private ZgCameraGLSurfaceView mGLSurfaceView;
    private BlockingQueue<ZgCameraFrameData> mImageDataBuffers;
    private boolean mIsSquare = false;
    private boolean mIsVisiable = true;

    public interface OnCameraListener {

        public boolean isSurfaceViewVisible();

        public boolean isSquare();
        /**
         * setup camera.
         * @return the camera
         */
        public Camera setupCamera();

        /**
         * reset on surfaceChanged.
         * @param format image format.
         * @param width width
         * @param height height.
         */
        public void setupChanged(int format, int width, int height);

        /**
         * start preview immediately, after surfaceCreated
         * @return true or false.
         */
        public boolean startPreviewLater();

        /**
         * on ui thread.
         * @param data image data
         * @param width  width
         * @param height height
         * @param format format
         * @param timestamp time stamp
         * @return image params.
         */
        public Object onPreview(byte[] data, int width, int height, int format, long timestamp);

        public void onBeforeRender(ZgCameraFrameData data);

        public void onAfterRender(ZgCameraFrameData data);
    }

    public ZgCameraSurfaceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
        onCreate();
    }

    public ZgCameraSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        onCreate();
    }

    public ZgCameraSurfaceView(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
        onCreate();
    }

    private void onCreate() {
        SurfaceHolder arg0 = getHolder();
        arg0.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        arg0.addCallback(this);

        mFrameHelper = new FrameHelper();
        mImageDataBuffers = new LinkedBlockingQueue<>();
        mGLSurfaceView = null;
    }

    private boolean openCamera() {
        try {
            if (mOnCameraListener != null) {
                mCamera = mOnCameraListener.setupCamera();
                mIsSquare = mOnCameraListener.isSquare();
            }

            if(mCamera != null){
                if(mIsSquare){
                    mWidth = CameraProxy.getInstance().getPreviewWidth();
                    mHeight = CameraProxy.getInstance().getPreviewHeight();
                    mFormat = CameraProxy.getInstance().getFormat();

                    int lineBytes = Math.min(mWidth,mHeight) * ImageFormat.getBitsPerPixel(mFormat) / 8;

                    if (mGLSurfaceView != null) {
                        mGLSurfaceView.setImageConfig(Math.min(mWidth,mHeight), Math.min(mWidth,mHeight), mFormat);
                        mGLSurfaceView.setAspectRatio(Math.min(mWidth,mHeight), Math.min(mWidth,mHeight));
                        mImageDataBuffers.offer(new ZgCameraFrameData(Math.min(mWidth,mHeight), Math.min(mWidth,mHeight), mFormat, lineBytes * Math.min(mWidth,mHeight)));
                        mImageDataBuffers.offer(new ZgCameraFrameData(Math.min(mWidth,mHeight), Math.min(mWidth,mHeight), mFormat, lineBytes * Math.min(mWidth,mHeight)));
                        mImageDataBuffers.offer(new ZgCameraFrameData(Math.min(mWidth,mHeight), Math.min(mWidth,mHeight), mFormat, lineBytes * Math.min(mWidth,mHeight)));
                    }
                } else {
                    mWidth = CameraProxy.getInstance().getPreviewWidth();
                    mHeight = CameraProxy.getInstance().getPreviewHeight();
                    mFormat = CameraProxy.getInstance().getFormat();

                    int lineBytes = mWidth * ImageFormat.getBitsPerPixel(mFormat) / 8;

                    if (mGLSurfaceView != null) {
                        mGLSurfaceView.setImageConfig(mWidth, mHeight, mFormat);
                        mGLSurfaceView.setAspectRatio(mWidth, mHeight);
                        mImageDataBuffers.offer(new ZgCameraFrameData(mWidth, mHeight, mFormat, lineBytes * mHeight));
                        mImageDataBuffers.offer(new ZgCameraFrameData(mWidth, mHeight, mFormat, lineBytes * mHeight));
                        mImageDataBuffers.offer(new ZgCameraFrameData(mWidth, mHeight, mFormat, lineBytes * mHeight));
                    }
                }
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // TODO Auto-generated method stub
        if (mOnCameraListener != null) {
            mOnCameraListener.setupChanged(format, width, height);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        openCamera();
        CameraProxy.getInstance().addZgCameraPreviewCallback(cb);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mImageDataBuffers.clear();
        CameraProxy.getInstance().removeZgCameraPreviewCallback(cb);
    }

    CameraProxy.ZgCameraPreviewCallback cb = new CameraProxy.ZgCameraPreviewCallback(){

        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            ZgCameraSurfaceView.this.onPreviewFrame(data, camera);
        }
    };

    public void onPreviewFrame(byte[] data, Camera camera) {
        long timestamp = System.nanoTime();
        mFrameHelper.printFPS();
        if (mGLSurfaceView != null) {
            ZgCameraFrameData imageData = mImageDataBuffers.poll();
            if (imageData != null) {
                byte[] buffer = imageData.mData;
                if(mIsSquare){
                    if(mOnCameraListener != null && mOnCameraListener.isSurfaceViewVisible()){
                        byte[] cropBuffer = new byte[buffer.length];
                        YuvUtil.cropYUV(data,
                                mWidth,
                                mHeight,
                                cropBuffer,
                                Math.min(mWidth,mHeight),
                                Math.min(mWidth,mHeight),
                                0,
                                0);
                        System.arraycopy(cropBuffer, 0, buffer, 0, buffer.length);
                        if (mOnCameraListener != null) {
                            imageData.mParams = mOnCameraListener.onPreview(buffer, Math.min(mWidth,mHeight), Math.min(mWidth,mHeight), mFormat, timestamp);
                        }
                        mGLSurfaceView.requestRender(imageData);
                    } else {
                        int lineBytes = Math.min(mWidth,mHeight) * ImageFormat.getBitsPerPixel(mFormat) / 8;
                        mImageDataBuffers.offer(new ZgCameraFrameData(Math.min(mWidth,mHeight), Math.min(mWidth,mHeight), mFormat, lineBytes * Math.min(mWidth,mHeight)));
                    }
                } else {
                    System.arraycopy(data, 0, buffer, 0, buffer.length);
                    if (mOnCameraListener != null) {
                        imageData.mParams = mOnCameraListener.onPreview(buffer, mWidth, mHeight, mFormat, timestamp);
                    }
                    mGLSurfaceView.requestRender(imageData);
                }

            }
        } else {
            if (mOnCameraListener != null) {
                if(mIsSquare){
                    mOnCameraListener.onPreview(data.clone(), mHeight, mHeight, mFormat, timestamp);
                } else {
                    mOnCameraListener.onPreview(data.clone(), mWidth, mHeight, mFormat, timestamp);
                }

            }
        }
    }


    @Override
    public void onBeforeRender(ZgCameraFrameData data) {
        if (mOnCameraListener != null) {
            data.mTimeStamp = System.nanoTime();
            mOnCameraListener.onBeforeRender(data);
        }
    }

    @Override
    public void onAfterRender(ZgCameraFrameData data) {
        if (mOnCameraListener != null) {
            data.mTimeStamp = System.nanoTime();
            mOnCameraListener.onAfterRender(data);
        }
        if (!mImageDataBuffers.offer(data)) {
            Log.e(TAG, "PREVIEW QUEUE FULL!");
        }
    }

    public void setOnCameraListener(OnCameraListener l) {
        mOnCameraListener = l;
    }

    public void setupGLSurfaceView(ZgCameraGLSurfaceView glv, boolean autofit, boolean mirror, int render_egree) {
        mGLSurfaceView = glv;
        mGLSurfaceView.setOnRenderListener(this);
        mGLSurfaceView.setRenderConfig(render_egree, mirror);
        mGLSurfaceView.setAutoFitMax(autofit);
    }

    public void debug_print_fps(boolean preview, boolean render) {
        if (mGLSurfaceView != null) {
            mGLSurfaceView.debug_print_fps(render);
        }
        mFrameHelper.enable(preview);
    }
}
