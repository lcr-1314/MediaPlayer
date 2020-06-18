package com.android.wllivepusher.camera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import com.android.wllivepusher.R;
import com.android.wllivepusher.egl.WLEGLSurfaceView;
import com.android.wllivepusher.egl.WlShaderUtil;
import com.android.wllivepusher.util.DisplayUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class WlCameraRender implements WLEGLSurfaceView.WlGLRender/*, SurfaceTexture.OnFrameAvailableListener*/{

    private Context context;

    private float[] vertexData = {
            -1f, -1f,
            1f, -1f,
            -1f, 1f,
            1f, 1f
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
    private int vboId;  // 顶点缓冲
    private int fboId;  // 离屏渲染

    private int fboTextureid;
    private int cameraTextureid;

    private int umatrix;
    private float[] matrix = new float[16];

    private SurfaceTexture surfaceTexture;
    private OnSurfaceCreateListener onSurfaceCreateListener;

    private WlCameraFboRender wlCameraFboRender;

    private int screenWidth = 1080;
    private int screenHeight = 1920;

    private int width;
    private int height;


    public WlCameraRender(Context context) {
        this.context = context;

        // 获取屏幕高宽
        screenWidth = DisplayUtil.getScreenWidth(context);
        screenHeight = DisplayUtil.getScreenHeight(context);

        wlCameraFboRender = new WlCameraFboRender(context); // 添加fbo离屏渲染
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

    public void setOnSurfaceCreateListener(OnSurfaceCreateListener onSurfaceCreateListener) {
        this.onSurfaceCreateListener = onSurfaceCreateListener;
    }

    @Override
    public void onSurfaceCreated() {
        // 离屏渲染
        wlCameraFboRender.onCreate();

        // 加载 顶点着色器 和 片元着色器
        String vertexSource = WlShaderUtil.getRawResource(context, R.raw.vertex_shader);
        String fragmentSource = WlShaderUtil.getRawResource(context, R.raw.fragment_shader);

        program = WlShaderUtil.createProgram(vertexSource, fragmentSource);
        vPosition = GLES20.glGetAttribLocation(program, "v_Position");
        fPosition = GLES20.glGetAttribLocation(program, "f_Position");
        umatrix = GLES20.glGetUniformLocation(program, "u_Matrix");

        // 1、顶点缓冲
        //vbo
        int [] vbos = new int[1];
        GLES20.glGenBuffers(1, vbos, 0);
        vboId = vbos[0]; // 顶点缓冲

        // 绑定顶点缓冲
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId);
        //分配vbo需要的缓存大小
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertexData.length * 4 + fragmentData.length * 4, null, GLES20. GL_STATIC_DRAW);
        //为vbo设置顶点数据的值 //先存入前8个字节，存入顶点坐标数据
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, 0, vertexData.length * 4, vertexBuffer);
        // 偏移8个字节后，在存入后8个字节，存入片元纹理坐标数据
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, vertexData.length * 4, fragmentData.length * 4, fragmentBuffer);
        // 使用完成后，解绑顶点缓冲
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        //vbo end

        // 2、帧缓冲
        //fbo
        int[] fbos = new int[1];
        GLES20.glGenBuffers(1, fbos, 0); // 创建fbo
        fboId = fbos[0];
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fboId); // 绑定fbo

        // 创建纹理(正常纹理)// 用来绑定fbo
        int []textureIds = new int[1];
        GLES20.glGenTextures(1, textureIds, 0);
        fboTextureid = textureIds[0];

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, fboTextureid);//绑定纹理(正在纹理)
        // 设置纹理的过虑和环绕(正常纹理)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        // 设置FBO分配内存大小 //只分配大小
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, screenWidth, screenHeight, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        // 把纹理绑定到FBO
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, fboTextureid, 0);
        //检查FBO是否绑定成功
        if (GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER) != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            Log.e("lcr", "fbo wrong");
        } else {
            Log.e("lcr", "fbo success");
        }
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);          // 解绑纹理
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0); // 解绑FBO

        //3、扩展纹理
        // 创建纹理
        int []textureidseos = new int[1];
        GLES20.glGenTextures(1, textureidseos, 0);
        cameraTextureid = textureidseos[0];
        // 绑定扩展纹理
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, cameraTextureid);
        // 设置扩展纹理的过虑和环绕
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        // 使用纹理创建surfaceTexture
        surfaceTexture = new SurfaceTexture(cameraTextureid);
        //surfaceTexture.setOnFrameAvailableListener(this);

        if (onSurfaceCreateListener != null) { // 回调到Camera
            onSurfaceCreateListener.onSurfaceCreate(surfaceTexture, fboTextureid);
        }
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0); // 解绑扩展纹理
    }

    public void resetMatrix() {
        Matrix.setIdentityM(matrix, 0);
    }

    public void setAngle(float angle, float x, float y, float z){
        Matrix.rotateM(matrix, 0, angle, x, y, z);
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public void onDrawFrame() {
        surfaceTexture.updateTexImage();//更新数据，更新一次，调用onFrameAvailable一次

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClearColor(1f,0f, 0f, 1f);

        GLES20.glUseProgram(program); //开始使用着色器程序
        GLES20.glViewport(0, 0, screenWidth, screenHeight);

        //resetMatrix();

        // 使用矩阵
        GLES20.glUniformMatrix4fv(umatrix, 1, false, matrix, 0);
        //Matrix.translateM(matrix, 0,0.1f, 0, 0);

        // 设置模式为fbo
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fboId); // 绑定帧缓冲
        // 设置模式为vbo
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId);     // 绑定顶点缓冲

        GLES20.glEnableVertexAttribArray(vPosition);        // 使能顶点数据
        //从0开始取8个字节                                    // 提取顶点坐标数据
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 8,
                0);
        GLES20.glEnableVertexAttribArray(fPosition);        // 使能纹理数据
        //从偏移的vertexData.length * 4开始，取8个字节         // 提取纹理坐标数据
        GLES20.glVertexAttribPointer(fPosition, 2, GLES20.GL_FLOAT, false, 8,
                vertexData.length * 4);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);    // 画四边形(两个三角形)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);              // 解绑纹理(正在纹理)

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);             // 解绑顶点缓冲数据

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);     // 解绑帧缓冲数据

        wlCameraFboRender.onChange(width, height);  // 以实际大小来绘制窗口
        wlCameraFboRender.onDraw(fboTextureid);     // 绘制窗口
    }

//    @Override
//    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
//
//    }

    public int getFboTextureid() {
        return fboTextureid;
    }

    public interface OnSurfaceCreateListener {
        void onSurfaceCreate(SurfaceTexture surfaceTexture, int textureId);
    }
}
