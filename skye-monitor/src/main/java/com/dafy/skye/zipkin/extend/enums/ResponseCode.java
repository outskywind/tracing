package com.dafy.skye.zipkin.extend.enums;

/**
 * Created by quanchengyun on 2018/4/11.
 */
public enum ResponseCode {

    SUCCESS("0"),
    SYSTEM_ERROR("系统错误")
    ,RULE_DUPLICATE("规则重复");

    private String value;

    ResponseCode(String value){
        this.value=value;
    }

    public String value(){
        return  this.value;
    }
}
