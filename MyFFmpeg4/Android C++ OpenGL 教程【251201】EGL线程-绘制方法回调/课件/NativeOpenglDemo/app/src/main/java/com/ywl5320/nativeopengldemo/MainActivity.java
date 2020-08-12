package com.ywl5320.nativeopengldemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.ywl5320.opengl.NativeOpengl;
import com.ywl5320.opengl.WlSurfaceView;

public class MainActivity extends AppCompatActivity {

    private WlSurfaceView wlSurfaceView;
    private NativeOpengl nativeOpengl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        wlSurfaceView = findViewById(R.id.wlsurfaceview);
        nativeOpengl = new NativeOpengl();
        wlSurfaceView.setNativeOpengl(nativeOpengl);
    }
}
