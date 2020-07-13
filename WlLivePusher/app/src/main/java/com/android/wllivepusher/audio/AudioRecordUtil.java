package com.android.wllivepusher.audio;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;

/* Android SDK 提供了两套音频采集的API，
   分别是：MediaRecorder 和 AudioRecord，
   前者是一个更加上层一点的API，它可以直接把手机麦克风录入的音频数据进行编码压缩（如AMR、MP3等）并存成文件，
   而后者则更接近底层，能够更加自由灵活地控制，可以得到原始的一帧帧PCM音频数据。*/

/*实现流程
    1、获取权限
    2、初始化获取每一帧流的Size
    3、初始化音频录制AudioRecord
        开始录制与保存录制音频文件
        停止录制
        给音频文件添加头部信息,并且转换格式成wav
    4、释放AudioRecord,录制流程完毕*/

public class AudioRecordUtil {

    private AudioRecord audioRecord;
    private int bufferSizeInBytes;
    private boolean start = false;
    private int readSize = 0;

    private OnRecordLisener onRecordLisener;

    public AudioRecordUtil() {
        //初始化获取每一帧流的Size //获取每一帧的字节流大小
        // https://www.cnblogs.com/guanxinjing/p/10969824.html
        bufferSizeInBytes = AudioRecord.getMinBufferSize(
                44100,                    // 采样率（赫兹）
                AudioFormat.CHANNEL_IN_STEREO,          // 声道配置 //此处为立体声
                AudioFormat.ENCODING_PCM_16BIT);        // 音频格式 表示音频数据的格式
        // 一般的手机设备可能只支持 16位PCM编码,如果其他的都会报错为坏值。
        //MediaRecorder.AudioSource.VOICE_COMMUNICATION,//
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION, // MediaRecorder.AudioSource.MIC,  // 音频源，这里选择使用麦克风
                44100,            //采样率（赫兹）  与前面初始化获取每一帧流的Size保持一致
                AudioFormat.CHANNEL_IN_STEREO,  //声道配置 描述音频声道的配置,例如左声道/右声道/前声道/后声道。   与前面初始化获取每一帧流的Size保持一致
                AudioFormat.ENCODING_PCM_16BIT, //音频格式  表示音频数据的格式。  与前面初始化获取每一帧流的Size保持一致
                bufferSizeInBytes               //缓存区大小,就是上面我们配置的AudioRecord.getMinBufferSize
        );
    }

    public void setOnRecordLisener(OnRecordLisener onRecordLisener) {
        this.onRecordLisener = onRecordLisener;
    }

    File pcmFile;
    FileOutputStream fileOutputStream = null;
    public void startRecord(final String pcmPath, final boolean bSaveFile, final boolean bSaveWav) {
        final String path = pcmPath + ".pcm";
        new Thread() {
            @Override
            public void run() {
                super.run();
                start = true;
                audioRecord.startRecording(); //开始录制
                byte[] audiodata = new byte[bufferSizeInBytes];

                try {
                    if (bSaveFile) {
                        pcmFile = new File(Environment.getExternalStorageDirectory()+File.separator + path);
                        fileOutputStream = new FileOutputStream(pcmFile);
                    }
                    while (start) {
                        // 读取流数据到audiodata数组中.
                        readSize = audioRecord.read(audiodata, 0, bufferSizeInBytes);
                        if(!bSaveFile) {
                            if (onRecordLisener != null) {
                                onRecordLisener.recordByte(audiodata, readSize);
                            }
                        }else{
                            fileOutputStream.write(audiodata);
                            fileOutputStream.flush();
                        }
                    }
                    fileOutputStream.flush();
                    fileOutputStream.close();

                    if (audioRecord != null) {
                        audioRecord.stop();
                        audioRecord.release();
                        audioRecord = null;
                    }

                    if (bSaveFile && bSaveWav) {
                        addHeadData(pcmPath);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    File handlerWavFile;
    private   void addHeadData(String path) {
        String wavPath = path + ".wav";
        handlerWavFile = new File(Environment.getExternalStorageDirectory() + File.separator + wavPath);
        PcmToWavUtil pcmToWavUtil = new PcmToWavUtil(44100, AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT);
        pcmToWavUtil.pcmToWav(pcmFile.toString(), handlerWavFile.toString());
    }


    public void stopRecord() {
        start = false;
    }

    public interface OnRecordLisener {
        void recordByte(byte[] audioData, int readSize);
    }

    public boolean isStart() {
        return start;
    }
}

