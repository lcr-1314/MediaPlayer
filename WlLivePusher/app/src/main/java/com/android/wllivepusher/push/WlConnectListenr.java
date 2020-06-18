package com.android.wllivepusher.push;

public interface WlConnectListenr {

    void onConnecting();

    void onConnectSuccess();

    void onConnectFail(String msg);

}
