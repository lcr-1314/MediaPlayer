
/**
 * Created by jianxi on 2017/5/24.
 * https://github.com/mabeijianxi
 * mabeijianxi@gmail.com
 */

#include "jx_media_muxer.h"
extern "C"
{
#include "jx_ffmpeg_cmd_run.h"
}
#include "jx_log.h"

int JXMediaMuxer::startMuxer( const char *in_filename_v, const char *in_filename_a,const char *out_filename) {


    size_t in_filename_v_size = strlen(in_filename_v);
    char *new_in_filename_v = (char *)malloc(in_filename_v_size+1);
    strcpy((new_in_filename_v), in_filename_v);

    size_t in_filename_a_size = strlen(in_filename_a);
    char *new_in_filename_a = (char *)malloc(in_filename_a_size+1);
    strcpy((new_in_filename_a), in_filename_a);

    size_t out_filename_size = strlen(out_filename);
    char *new_out_filename = (char *)malloc(out_filename_size+1);
    strcpy((new_out_filename), out_filename);


    LOGI(JNI_DEBUG,"视音编码成功,开始合成")
    char *cmd[10];
    cmd[0]="ffmpeg";
    cmd[1]="-i";
    cmd[2]=new_in_filename_v;
    cmd[3]="-i";
    cmd[4]=new_in_filename_a;
    cmd[5]="-c:v";
    cmd[6]="copy";
    cmd[7]="-c:a";
    cmd[8]="copy";
    cmd[9]=new_out_filename;
    return ffmpeg_cmd_run(10,cmd);
}