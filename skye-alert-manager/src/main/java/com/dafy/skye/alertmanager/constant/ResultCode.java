package com.dafy.skye.alertmanager.constant;

public enum ResultCode {

    PARAM_ERROR("1", "参数有误");

    private final String code;
    private final String desc;

    ResultCode(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
