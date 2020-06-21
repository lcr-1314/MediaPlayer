package com.android.wllivepusher;

import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.wllivepusher.camera.WlCameraView;

public class CameraActivity extends AppCompatActivity {

    private WlCameraView wlCameraView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        wlCameraView = findViewById(R.id.cameraview);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        wlCameraView.onDestory();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        wlCameraView.previewAngle(CameraActivity.this);
    }
}
