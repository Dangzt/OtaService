package com.soundai.upgrading.bean;

public enum EventType {
    EVENT_CLOSE,//关闭事件
    BOOT_COMPLETE,//启动成功
    NONE_NETWORK,//无网
    NONE_LOGIN, //用户未登录
    DISABLE_MIC,//禁用MIC
    DOWNLOADING_OTA,//下载OTA
    DOWNLOADING_COMPLETE,//OTA下载完成
    VOLIME_EVENT_DOWN; //音量键按下
    private String typeName;

    EventType(String name) {
        this.typeName = name;
    }

    EventType() {
        this.typeName = this.toString();
    }
}
