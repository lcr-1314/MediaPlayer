package com.rayman.rmcamera;

import android.content.Context;
import android.graphics.Rect;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class CustomSurfaceView extends SurfaceView {

    private ONTouchEvent mTouchEvent;

    public CustomSurfaceView(Context context) {
        super(context);
    }

    public CustomSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (MotionEvent.ACTION_DOWN == event.getAction()) {
            mTouchEvent.onTouchEvent(event);
        }
        return super.onTouchEvent(event);
    }

    public void setCustomEvent(ONTouchEvent onTouchEvent) {
        mTouchEvent = onTouchEvent;
    }

    public interface ONTouchEvent {
        public void onTouchEvent(MotionEvent event);
    }

    /**
     * 转换对焦区域
     * 范围(-1000, -1000, 1000, 1000)
     * x,y是坐标位置，width,height SurfaceView的宽高，coefficient是区域比例大小
     */
    private  Rect calculateTapArea(float x, float y,  int width, int height, float coefficient) {
        float focusAreaSize = 200;
        //这段代码可以看出coefficient的作用，只是为了扩展areaSize。
        int areaSize = (int) (focusAreaSize * coefficient);
        int surfaceWidth = width;
        int surfaceHeight = height;
        //解释一下为什么*2000，因为要把surfaceView的坐标转换为范围(-1000, -1000, 1000, 1000)，则SurfaceView的中心点坐标会转化为（0,0），x/surfaceWidth ，得到当前x坐标占总宽度的比例，然后乘以2000就换算成了（0,0，2000,2000）的坐标范围内，然后减去1000，就换算为了范围(-1000, -1000, 1000, 1000)的坐标。
        //得到了x,y转换后的坐标，利用areaSize就可以得到聚焦区域。
        int centerX = (int) (x / surfaceHeight * 2000 - 1000);
        int centerY = (int) (y / surfaceWidth * 2000 - 1000);
        int left = clamp(centerX - (areaSize / 2), -1000, 1000);
        int top = clamp(centerY - (areaSize / 2), -1000, 1000);
        int right = clamp(left + areaSize, -1000, 1000);
        int bottom = clamp(top + areaSize, -1000, 1000);
        return new Rect(left, top, right, bottom);
    }
    //不大于最大值，不小于最小值
    private  int clamp(int x, int min, int max) {
        if (x > max) {
            return max;
        }
        if (x < min) {
            return min;
        }
        return x;
    }

//    mSurfaceView.setCustomEvent(new CustomSurfaceView.ONTouchEvent() {
//    @Override
//    public void onTouchEvent(MotionEvent event) {
//            handleFocus(event, mCamera);
//            }
//            });
}

//    private  void handleFocus(MotionEvent event, Camera camera) {
//        int viewWidth = useWidth;
//        int viewHeight = useHeight;
//        Rect focusRect = calculateTapArea(event.getX(), event.getY(),  viewWidth, viewHeight,1.0f);
//        //一定要首先取消，否则无法再次开启
//        camera.cancelAutoFocus();
//        Camera.Parameters params = camera.getParameters();
//        if (params.getMaxNumFocusAreas() > 0) {
//            List<Camera.Area> focusAreas = new ArrayList<>();
//            focusAreas.add(new Camera.Area(focusRect, 800));
//            params.setFocusAreas(focusAreas);
//        } else {
//            //focus areas not supported
//        }
//
//    //首先保存原来的对焦模式，然后设置为macro，对焦回调后设置为保存的对焦模式
//    final String currentFocusMode = params.getFocusMode();
//    params.setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO);
//    camera.setParameters(params);
//
//    camera.autoFocus(new Camera.AutoFocusCallback() {
//        @Override
//        public void onAutoFocus(boolean success, Camera camera) {
//            //回调后 还原模式
//            Camera.Parameters params = camera.getParameters();
//            params.setFocusMode(currentFocusMode);
//            camera.setParameters(params);
////        if(success){
////        Toast.makeText(Cu.this,"对焦区域对焦成功", Toast.LENGTH_SHORT).show();
////        }
//        }
//    });
