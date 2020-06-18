
#include "RtmpPush.h"

RtmpPush::RtmpPush(const char *url, WlCallJava *wlCallJava) {
    this->url = static_cast<char *>(malloc(512));
    strcpy(this->url, url);
    this->queue = new WlQueue();
    this->wlCallJava = wlCallJava;
}

RtmpPush::~RtmpPush() {
    queue->notifyQueue();
    queue->clearQueue();
    free(url);
}

void *callBackPush(void *data) {
    RtmpPush *rtmpPush = static_cast<RtmpPush *>(data);
    rtmpPush->startPushing = false;
    rtmpPush->rtmp = RTMP_Alloc();
    RTMP_Init(rtmpPush->rtmp);
    rtmpPush->rtmp->Link.timeout = 10;
    rtmpPush->rtmp->Link.lFlags |= RTMP_LF_LIVE;
    RTMP_SetupURL(rtmpPush->rtmp, rtmpPush->url);
    RTMP_EnableWrite(rtmpPush->rtmp);

    if (!RTMP_Connect(rtmpPush->rtmp, NULL)) {
        rtmpPush->wlCallJava->onConnectFail("can not connect the url");
        goto end;
    }
    if (!RTMP_ConnectStream(rtmpPush->rtmp, 0)) {
        rtmpPush->wlCallJava->onConnectFail("can not connect the stream of service");
        goto end;
    }
    rtmpPush->wlCallJava->onConnectsuccess();
    rtmpPush->startPushing = true;
    rtmpPush->startTime = RTMP_GetTime();

    while (true) {
        if (!rtmpPush->startPushing) {
            break;
        }

        RTMPPacket *packet = NULL;
        packet = rtmpPush->queue->getRtmpPacket();
        if (packet != NULL) {
            int result = RTMP_SendPacket(rtmpPush->rtmp, packet, 1);
            LOGD("RTMP_SendPacket result is %d", result);
            RTMPPacket_Free(packet);
            free(packet);
            packet = NULL;
        }
    }
    end:
    RTMP_Close(rtmpPush->rtmp);
    RTMP_Free(rtmpPush->rtmp);
    rtmpPush->rtmp = NULL;
    pthread_exit(&rtmpPush->push_thread);
}

void RtmpPush::init() {
    wlCallJava->onConnectint(WL_THREAD_MAIN);
    pthread_create(&push_thread, NULL, callBackPush, this);
}

// 拼装头部 // 封装RTMPPacket
void RtmpPush::pushSPSPPS(char *sps, int sps_len, char *pps, int pps_len) {
    int bodysize = sps_len + pps_len + 16;//sps和pps长度加上额外16字节长度
    RTMPPacket *packet = static_cast<RTMPPacket *>(malloc(sizeof(RTMPPacket)));
    RTMPPacket_Alloc(packet, bodysize);
    RTMPPacket_Reset(packet);

    char *body = packet->m_body;

    int i = 0;
    // H.264标准
    body[i++] = 0x17;       //frame type 1:关键帧 4bit 2:非关键帧  CodecID: 7 表示AVC(4bit)

    body[i++] = 0x00;       // fixed:0x00 0x00 0x00 0x00 (4byte)
    body[i++] = 0x00;
    body[i++] = 0x00;
    body[i++] = 0x00;

    body[i++] = 0x01;       // 版本
    body[i++] = sps[1];     // Profile
    body[i++] = sps[2];     // 兼容性
    body[i++] = sps[3];     // Profile levels

    body[i++] = 0xFF;       // 包长数据所使用的字节数

    body[i++] = 0xE1;       // sps个数
    body[i++] = (sps_len >> 8) & 0xff;  // sps长度（2 byte）// 高8位
    body[i++] = sps_len & 0xff;                            // 低8位
    memcpy(&body[i], sps, sps_len);     // sps实际内容
    i += sps_len;

    body[i++] = 0x01;                   // pps个数
    body[i++] = (pps_len >> 8) & 0xff;  // pps长度（2 byte） // 高8位
    body[i++] = pps_len & 0xff;                             // 低8位
    memcpy(&body[i], pps, pps_len);     // pps实际内容

    packet->m_packetType = RTMP_PACKET_TYPE_VIDEO;
    packet->m_nBodySize = bodysize;
    packet->m_nTimeStamp = 0;           // 直播，每次时间从0开始
    packet->m_hasAbsTimestamp = 0;      // 直播，每次时间从0开始
    packet->m_nChannel = 0x04;          // 声音/视频通道
    packet->m_headerType = RTMP_PACKET_SIZE_MEDIUM;
    packet->m_nInfoField2 = rtmp->m_stream_id;  // 流的id.

    queue->putRtmpPacket(packet);
}

void RtmpPush::pushVideoData(char *data, int data_len, bool keyframe) {
    int bodysize = data_len + 9;
    RTMPPacket *packet = static_cast<RTMPPacket *>(malloc(sizeof(RTMPPacket)));
    RTMPPacket_Alloc(packet, bodysize);
    RTMPPacket_Reset(packet);

    char *body = packet->m_body;
    int i = 0;

    if (keyframe) {
        body[i++] = 0x17;// frame type：1关键帧     // CodecId:7表示AVC(4 bit)
    } else {
        body[i++] = 0x27;// frame type：2非关键帧   // CodecId:7表示AVC(4 bit)
    }

    body[i++] = 0x01;    // fixed:0x01 0x00 0x00 0x00 (4 bit)
    body[i++] = 0x00;
    body[i++] = 0x00;
    body[i++] = 0x00;

    body[i++] = (data_len >> 24) & 0xff; // data length：长度信息(4 bit)
    body[i++] = (data_len >> 16) & 0xff;
    body[i++] = (data_len >> 8) & 0xff;
    body[i++] = data_len & 0xff;

    memcpy(&body[i], data, data_len);   // H264 裸数据

    packet->m_packetType = RTMP_PACKET_TYPE_VIDEO;
    packet->m_nBodySize = bodysize;
    packet->m_nTimeStamp = RTMP_GetTime() - startTime;
    packet->m_hasAbsTimestamp = 0;
    packet->m_nChannel = 0x04;
    packet->m_headerType = RTMP_PACKET_SIZE_LARGE;
    packet->m_nInfoField2 = rtmp->m_stream_id;

    queue->putRtmpPacket(packet);
}

void RtmpPush::pushAudioData(char *data, int data_len) {
    int bodysize = data_len + 2;
    RTMPPacket *packet = static_cast<RTMPPacket *>(malloc(sizeof(RTMPPacket)));
    RTMPPacket_Alloc(packet, bodysize);
    RTMPPacket_Reset(packet);
    char *body = packet->m_body;
    body[0] = 0xAF;     // ACC数据参数信息
    body[1] = 0x01;     // ACC头信息，或者ACC原始数据
    memcpy(&body[2], data, data_len);

    packet->m_packetType = RTMP_PACKET_TYPE_AUDIO;
    packet->m_nBodySize = bodysize;
    packet->m_nTimeStamp = RTMP_GetTime() - startTime;
    packet->m_hasAbsTimestamp = 0;
    packet->m_nChannel = 0x04;
    packet->m_headerType = RTMP_PACKET_SIZE_LARGE;
    packet->m_nInfoField2 = rtmp->m_stream_id;
    queue->putRtmpPacket(packet);
}

void RtmpPush::pushStop() {
    startPushing = false;
    queue->notifyQueue();
    pthread_join(push_thread, NULL);
}
