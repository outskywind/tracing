package com.dafy.skye.alertmanager.constant;

public enum NotificationChannel {

    /**
     * 企业微信
     */
    WECHAT(1),

    /**
     * 邮件
     */
    EMAIL(2),

    /**
     * 手机短信
     */
    SMS(3),

    /**
     * tapd
     */
    TAPD(4);

    private final int value;

    NotificationChannel(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
