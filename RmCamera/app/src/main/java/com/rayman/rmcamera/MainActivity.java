package com.rayman.rmcamera;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback {
    private static int mOrientation = 0;
    private static int mCameraID = Camera.CameraInfo.CAMERA_FACING_BACK;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private Camera mCamera;
    private boolean havePermission = false;
    private Button btnFocus;
    private Button btnTakePic;
    private Button btnRestar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnFocus = findViewById(R.id.focus);
        btnTakePic = findViewById(R.id.takepic);
        btnRestar = findViewById(R.id.restar);

        btnRestar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mCamera != null){
                    mCamera.startPreview();
                }
            }
        });
        btnFocus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        btnTakePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mCamera!= null){
                    mCamera.takePicture(null, null, new Camera.PictureCallback() {
                        @Override
                        public void onPictureTaken(byte[] data, Camera camera) {
                            Log.d("", "=======================onPictureTaken");
                            mCamera.addCallbackBuffer(data);
//                            // 获取Jpeg图片，并保存在sd卡上
//                            String path = Environment.getExternalStorageDirectory()
//                                    .getPath()  +"/focus/";
//                            File pathDir = new File(path);
//                            if (!pathDir.exists()){
//                                pathDir.mkdir();
//                            }
//                            File pictureFile = new File(path+ "focusdemo.jpg");
//                            if (pictureFile.exists()){
//                                pictureFile.delete();
//                            }
//                            try {
//                                FileOutputStream fos = new FileOutputStream(pictureFile);
//                                fos.write(data);
//                                fos.close();
//                            } catch (Exception e) {
//
//                            }
                        }
                    });
                }
            }
        });

        // Android 6.0相机动态权限检查,省略了
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {
            havePermission = true;
            init(); // 初始化
        } else {
            havePermission = false;
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.CAMERA,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    }, 100);
        }
    }

    public void init(){
        if(mSurfaceView == null){
            mSurfaceView = findViewById(R.id.surfaceview);
//            mSurfaceView.setCustomEvent(new CustomSurfaceView.ONTouchEvent() {
//                @Override
//                public void onTouchEvent(MotionEvent event) {
//                    handleFocus(event, mCamera);
//                }
//            });
            mSurfaceHolder = mSurfaceView.getHolder();
            mSurfaceHolder.addCallback(this);
            WindowManager wm = (WindowManager) MainActivity.this.getSystemService(Context.WINDOW_SERVICE);
            int width = wm.getDefaultDisplay().getWidth();
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) mSurfaceView.getLayoutParams();
            layoutParams.width = width;
            layoutParams.height = width*4/3;
//            useWidth = width;
//            useHeight = width*4/3;
            mSurfaceView.setLayoutParams(layoutParams);
        }

    }

    private Camera.Size getFitSize(List<Camera.Size> sizes) {
//        if (width < height) {
//            int t = height;
//            height = width;
//            width = t;
//        }
//
//        for (Camera.Size size : sizes) {
//            if (1.0f * size.width / size.height == 1.0f * width / height) {
//                return size;
//            }
//        }
        return sizes.get(0);
    }


    private void initCamera() {
        if (mCamera != null){
            releaseCamera();
            System.out.println("===================releaseCamera=============");
        }
        mCamera = Camera.open(mCameraID);
        System.out.println("===================openCamera=============");
        if (mCamera != null){
            try {
                mCamera.setPreviewDisplay(mSurfaceHolder);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Camera.Parameters parameters = mCamera.getParameters();
            Camera.Size size = getFitSize(parameters.getSupportedPictureSizes());
            parameters.setPictureSize(size.width, size.height);

            size = getFitSize(parameters.getSupportedPreviewSizes());
            parameters.setPreviewSize(size.width, size.height);


            parameters.setRecordingHint(true);

            //设置获取数据
            parameters.setPreviewFormat(ImageFormat.NV21);
            //parameters.setPreviewFormat(ImageFormat.YUV_420_888);

            //通过setPreviewCallback方法监听预览的回调：
            mCamera.setPreviewCallback(new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] bytes, Camera camera) {
                    //这里面的Bytes的数据就是NV21格式的数据,或者YUV_420_888的数据
                    Log.d("adf", "===================onPreviewFrame");
                }
            });

            if(mCameraID == Camera.CameraInfo.CAMERA_FACING_BACK){
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            }

            mCamera.setParameters(parameters);

//            calculateCameraPreviewOrientation(MainActivity.this);
//            Camera.Size tempSize = setPreviewSize(mCamera, 1080,1920);
//            {
//                //此处可以处理，获取到tempSize，如果tempSize和设置的SurfaceView的宽高冲突，重新设置SurfaceView的宽高
//            }

//            setPictureSize(mCamera,  1080,1920);
            mCamera.setDisplayOrientation(mOrientation);
            int degree = calculateCameraPreviewOrientation(MainActivity.this);
            mCamera.setDisplayOrientation(degree);
            mCamera.startPreview();
        }
    }

    public void releaseCamera(){
        if (mCamera != null) {
            mSurfaceHolder.removeCallback(this);
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.lock();
            mCamera.release();
            mCamera = null;

        }
    }
    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        //当SurfaceView变化时也需要做相应操作，这里未做相应操作
        if (havePermission){
            initCamera();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mCamera.stopPreview();
    }

    private void setPictureSize(Camera camera ,int expectWidth,int expectHeight){
        Camera.Parameters parameters = camera.getParameters();
        Point point = new Point(expectWidth, expectHeight);
        Camera.Size size = findProperSize(point, parameters.getSupportedPreviewSizes());
        parameters.setPictureSize(size.width, size.height);
        camera.setParameters(parameters);
    }

    private Camera.Size setPreviewSize(Camera camera, int expectWidth, int expectHeight) {
        Camera.Parameters parameters = camera.getParameters();
        Point point = new Point(expectWidth, expectHeight);
        Camera.Size size = findProperSize(point,parameters.getSupportedPictureSizes());
        parameters.setPictureSize(size.width, size.height);
        camera.setParameters(parameters);
        return size;
    }

    /**
     * 找出最合适的尺寸，规则如下：
     * 1.将尺寸按比例分组，找出比例最接近屏幕比例的尺寸组
     * 2.在比例最接近的尺寸组中找出最接近屏幕尺寸且大于屏幕尺寸的尺寸
     * 3.如果没有找到，则忽略2中第二个条件再找一遍，应该是最合适的尺寸了
     */
    private static Camera.Size findProperSize(Point surfaceSize, List<Camera.Size> sizeList) {
        if (surfaceSize.x <= 0 || surfaceSize.y <= 0 || sizeList == null) {
            return null;
        }

        int surfaceWidth = surfaceSize.x;
        int surfaceHeight = surfaceSize.y;

        List<List<Camera.Size>> ratioListList = new ArrayList<>();
        for (Camera.Size size : sizeList) {
            addRatioList(ratioListList, size);
        }

        final float surfaceRatio = (float) surfaceWidth / surfaceHeight;
        List<Camera.Size> bestRatioList = null;
        float ratioDiff = Float.MAX_VALUE;
        for (List<Camera.Size> ratioList : ratioListList) {
            float ratio = (float) ratioList.get(0).width / ratioList.get(0).height;
            float newRatioDiff = Math.abs(ratio - surfaceRatio);
            if (newRatioDiff < ratioDiff) {
                bestRatioList = ratioList;
                ratioDiff = newRatioDiff;
            }
        }

        Camera.Size bestSize = null;
        int diff = Integer.MAX_VALUE;
        assert bestRatioList != null;
        for (Camera.Size size : bestRatioList) {
            int newDiff = Math.abs(size.width - surfaceWidth) + Math.abs(size.height - surfaceHeight);
            if (size.height >= surfaceHeight && newDiff < diff) {
                bestSize = size;
                diff = newDiff;
            }
        }

        if (bestSize != null) {
            return bestSize;
        }

        diff = Integer.MAX_VALUE;
        for (Camera.Size size : bestRatioList) {
            int newDiff = Math.abs(size.width - surfaceWidth) + Math.abs(size.height - surfaceHeight);
            if (newDiff < diff) {
                bestSize = size;
                diff = newDiff;
            }
        }

        return bestSize;
    }

    private static void addRatioList(List<List<Camera.Size>> ratioListList, Camera.Size size) {
        float ratio = (float) size.width / size.height;
        for (List<Camera.Size> ratioList : ratioListList) {
            float mine = (float) ratioList.get(0).width / ratioList.get(0).height;
            if (ratio == mine) {
                ratioList.add(size);
                return;
            }
        }

        List<Camera.Size> ratioList = new ArrayList<>();
        ratioList.add(size);
        ratioListList.add(ratioList);
    }
    /**
     * 排序
     * @param list
     */
    private static void sortList(List<Camera.Size> list) {
        Collections.sort(list, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size pre, Camera.Size after) {
                if (pre.width > after.width) {
                    return 1;
                } else if (pre.width < after.width) {
                    return -1;
                }
                return 0;
            }
        });
    }

    /**
     * 设置预览角度，setDisplayOrientation本身只能改变预览的角度
     * previewFrameCallback以及拍摄出来的照片是不会发生改变的，拍摄出来的照片角度依旧不正常的
     * 拍摄的照片需要自行处理
     * 这里Nexus5X的相机简直没法吐槽，后置摄像头倒置了，切换摄像头之后就出现问题了。
     * @param activity
     */
    public static int calculateCameraPreviewOrientation(Activity activity) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(mCameraID, info);
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;
        } else {
            result = (info.orientation - degrees + 360) % 360;
        }
        mOrientation = result;
        System.out.println("=========orienttaion============="+result);
        return result;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (havePermission && mCamera != null)
            mCamera.startPreview();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (havePermission && mCamera != null)
            mCamera.stopPreview();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            // 相机权限
            case 100:
                havePermission = true;
                init();
                break;
        }
    }
}
