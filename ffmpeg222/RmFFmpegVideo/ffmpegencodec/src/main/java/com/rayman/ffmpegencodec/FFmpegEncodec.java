package com.rayman.ffmpegencodec;

public class FFmpegEncodec {
    static {
        System.loadLibrary("native-lib");
    }

    public FFmpegEncodec() {
    }

//    public String getTextStr(){
//        return stringFromJNI();
//    }

    //private native String stringFromJNI();
}
