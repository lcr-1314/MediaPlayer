package com.android.wllivepusher.push;

import android.content.Context;

import com.android.wllivepusher.encodec.WlBaseMediaEncoder;

//
public class WlPushEncodec extends WlBasePushEncoder{

    private WlEncodecPushRender wlEncodecPushRender;

    public WlPushEncodec(Context context, int textureId) {
        super(context);

        // 渲染推流解码界面 // renderer // 他的surface将来自于 MediaCodec.createInputSurface().
        wlEncodecPushRender = new WlEncodecPushRender(context, textureId);
        setRender(wlEncodecPushRender);

        setmRenderMode(WlBaseMediaEncoder.RENDERMODE_CONTINUOUSLY);
    }
}
