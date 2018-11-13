package com.dafy.skye.alertmanager.constant;

public enum WarningLevel {
    /*
     * 这里列出的告警级别必须要由低到高依次排序，业务代码中会根据枚举的位置来判断优先级，
     * 而不是为每一个枚举手动指定一个整数，这样不利于扩展和维护。
     */

    low,
    middle,
    high
}
