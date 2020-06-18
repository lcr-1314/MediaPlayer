package com.android.wllivepusher.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;


import com.android.wllivepusher.R;
import com.android.wllivepusher.egl.WlShaderUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class WlCameraFboRender {

    private Context context;

    private float[] vertexData = {
            -1f, -1f,
            1f, -1f,
            -1f, 1f,
            1f, 1f,

            0f, 0f,
            0f, 0f,
            0f, 0f,
            0f, 0f
    };
    private FloatBuffer vertexBuffer;

    private float[] fragmentData = {
            0f, 1f,
            1f, 1f,
            0f, 0f,
            1f, 0f
    };
    private FloatBuffer fragmentBuffer;

    private int program;
    private int vPosition;
    private int fPosition;
    private int textureid;
    private int sampler;

    private int umatrix;
    private float[] matrix = new float[16];

    private int vboId;

    private Bitmap bitmap;

    private int bitmapTextureid;

    public WlCameraFboRender(Context context) {
        this.context = context;

        bitmap = WlShaderUtil.createTextImage("我的水印", 50, "#ff0000", "#00000000", 0);

        float r = 1.0f * bitmap.getWidth() / bitmap.getHeight();
        float w = r * 0.1f;

        vertexData[8] = 0.8f - w; //左上角
        vertexData[9] = -0.8f;

        vertexData[10] = 0.8f;   //右下角
        vertexData[11] = -0.8f;

        vertexData[12] = 0.8f - w; //左上角
        vertexData[13] = -0.7f;

        vertexData[14] = 0.8f; //右上角
        vertexData[15] = -0.7f;

        Log.d("TAG", "======");

//        vertexData[8] = -w;//左上角
//        vertexData[9] = w;
//
//        vertexData[10] = -w;//左下角
//        vertexData[11] = -w;
//
//        vertexData[12] = w; //右上角
//        vertexData[13] = w;
//
//        vertexData[14] = w; //右下角
//        vertexData[15] = -w;

        vertexBuffer = ByteBuffer.allocateDirect(vertexData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(vertexData);
        vertexBuffer.position(0);

        fragmentBuffer = ByteBuffer.allocateDirect(fragmentData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(fragmentData);
        fragmentBuffer.position(0);

    }

    public void onCreate() {
        // 透明
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        String vertexSource = WlShaderUtil.getRawResource(context, R.raw.vertex_shader);
        String fragmentSource = WlShaderUtil.getRawResource(context, R.raw.fragment_shader_screen);

        program = WlShaderUtil.createProgram(vertexSource, fragmentSource);

        vPosition = GLES20.glGetAttribLocation(program, "v_Position");
        fPosition = GLES20.glGetAttribLocation(program, "f_Position");
        sampler = GLES20.glGetUniformLocation(program, "sTexture");

        umatrix = GLES20.glGetUniformLocation(program, "u_Matrix");

        // vbo 顶点缓冲
        int[] vbos = new int[1];
        GLES20.glGenBuffers(1, vbos, 0);
        vboId = vbos[0];

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertexData.length * 4 + fragmentData.length * 4, null, GLES20.GL_STATIC_DRAW);
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, 0, vertexData.length * 4, vertexBuffer);
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, vertexData.length * 4, fragmentData.length * 4, fragmentBuffer);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        bitmapTextureid = WlShaderUtil.loadBitmapTexture(bitmap);

        Matrix.setIdentityM(matrix, 0);
    }

    public void onChange(int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }

    public void onDraw(int textureId) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClearColor(1f, 0f, 0f, 1f);

        GLES20.glUseProgram(program);

//        Matrix.setIdentityM(matrix, 0);
//        // 使用矩阵
        GLES20.glUniformMatrix4fv(umatrix, 1, false, matrix, 0);


        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId); // 绑定顶点缓冲

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);

        GLES20.glEnableVertexAttribArray(vPosition);
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 8,
                0);
        GLES20.glEnableVertexAttribArray(fPosition);
        GLES20.glVertexAttribPointer(fPosition, 2, GLES20.GL_FLOAT, false, 8,
                vertexData.length * 4);
        //绘图
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        //bitmap
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, bitmapTextureid);// 绑定纹理
        // 顶点缓冲设置纹理
        GLES20.glEnableVertexAttribArray(vPosition);
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 8,
                32);
        GLES20.glEnableVertexAttribArray(fPosition);
        GLES20.glVertexAttribPointer(fPosition, 2, GLES20.GL_FLOAT, false, 8,
                vertexData.length * 4);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);


        if (!isre) {
            offsetx += 0.05f;
            if (offsetx > 1) {
                isre = true;
            }
        }
        if (isre) {
            offsetx -= 0.01f;
            if (offsetx < 0) {
                isre = false;
            }
        }

        pushMatrix();
        Matrix.translateM(matrix, 0, offsetx, 0, 0);
//        Matrix.rotateM(matrix, 0, offsetx, 0, 0, 1);
//         Matrix.scaleM(matrix,0, offsetx, offsetx, 0);
        GLES20.glUniformMatrix4fv(umatrix, 1, false, matrix, 0);
        // 绘图//水印
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        popMatrix();
    }

    boolean isre = false;
    float offsetx = 1f;

    float[][] mStack=new float[10][16];
    static int stackTop=-1;
    public void pushMatrix() {
        stackTop++;
        for (int i = 0; i < 16; i++) {
            mStack[stackTop][i] = matrix[i];
        }
    }

    public void popMatrix() {
        for (int i = 0; i < 16; i++) {
            matrix[i] = mStack[stackTop][i];
        }
        stackTop--;
    }
}
