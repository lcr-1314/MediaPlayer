package com.rayman.rmffmpegvideo.model;

public class AutoVBRModel extends BaseMediaBitrateConfig {

    public AutoVBRModel(){
        this.mode= MODE.AUTO_VBR;
    }

    /**
     *
     * @param crfSize 压缩等级，0~51，值越大约模糊，视频越小，建议18~28.
     */
    public AutoVBRModel(int crfSize){
        if(crfSize<0||crfSize>51){
            throw  new IllegalArgumentException("crfSize 在0~51之间");
        }
        this.crfSize=crfSize;
        this.mode= MODE.AUTO_VBR;
    }
}
