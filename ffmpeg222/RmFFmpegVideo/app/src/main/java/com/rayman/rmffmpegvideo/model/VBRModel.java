package com.rayman.rmffmpegvideo.model;

public class VBRModel extends BaseMediaBitrateConfig {
    /**
     *
     * @param maxBitrate 最大码率
     * @param bitrate 额定码率
     */
    public VBRModel(int maxBitrate, int bitrate){
        if(maxBitrate<=0||bitrate<=0){
            throw new IllegalArgumentException("maxBitrate or bitrate value error!");
        }
        this.maxBitrate=maxBitrate;
        this.bitrate=bitrate;
        this.mode= MODE.VBR;
    }
}
