package com.rh.camera.surface;

/**
 * @author yang yingwei
 * @description
 * @date 2018/7/17
 * @modify
 */

public class ZgCameraFrameData {
    //image data.
    byte[] mData;
    int mWidth, mHeight, mFormat;

    //user data
    Object mParams;
    //timestamp
    long mTimeStamp;

    public ZgCameraFrameData(int w, int h, int f, int size) {
        mWidth = w;
        mHeight = h;
        mFormat = f;
        mData = new byte[size];
        mParams = null;
        mTimeStamp = System.nanoTime();
    }

    public byte[] getData() {
        return mData;
    }

    public void setData(byte[] mData) {
        this.mData = mData;
    }

    public Object getParams() {
        return mParams;
    }

    public void setParams(Object mParams) {
        this.mParams = mParams;
    }
}
