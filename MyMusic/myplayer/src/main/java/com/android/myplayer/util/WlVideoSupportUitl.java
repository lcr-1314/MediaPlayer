package com.android.myplayer.util;

import android.media.MediaCodecList;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class WlVideoSupportUitl {
    private final static String TAG = "WlVideoSupportUitl";

    private static Map<String, String> codecMap = new HashMap<>();
    static {
        codecMap.put("h264", "video/avc");
        codecMap.put("h265", "video/hevc");
        codecMap.put("h263", "video/3gpp");
        codecMap.put("mpeg4", "video/mp4v-es");
        codecMap.put("rv40", "");
    }

    public static String findVideoCodecName(String ffcodename) {
        if (codecMap.containsKey(ffcodename)) {
            return codecMap.get(ffcodename);
        }
        return "";
    }

    public static boolean isSupportCodec(String ffcodecname) {
        boolean supportvideo = false;
        int count = MediaCodecList.getCodecCount();
        Log.d(TAG, "=============count:" + count);
        for (int i = 0; i < count; i++) {
            String[] tyeps = MediaCodecList.getCodecInfoAt(i).getSupportedTypes();
            Log.d(TAG, "=============tyeps.length:" + tyeps.length);
            for (int j = 0; j < tyeps.length; j++) {
                Log.d(TAG, "=============tyeps[" + j + "]:" + tyeps[j]);
                if (tyeps[j].equals(findVideoCodecName(ffcodecname))) {
                    supportvideo = true;
                    break;
                }
            }

            if (supportvideo) {
                break;
            }
        }
        return supportvideo;
    }
}
