package com.rayman.rmffmpegvideo.model;

public class CBRModel extends BaseMediaBitrateConfig {

    /**
     *
     * @param bufSize
     * @param bitrate 固定码率值
     */
    public CBRModel(int bufSize, int bitrate){
        if(bufSize<=0||bitrate<=0){
            throw new IllegalArgumentException("bufSize or bitrate value error!");
        }
        this.bufSize=bufSize;
        this.bitrate=bitrate;
        this.mode= MODE.CBR;
    }
}
