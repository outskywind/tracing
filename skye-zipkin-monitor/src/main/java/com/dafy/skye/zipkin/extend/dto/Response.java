package com.dafy.skye.zipkin.extend.dto;

/**
 * Created by quanchengyun on 2018/4/11.
 */
public class Response<T> {

    String code;

    T result;

    public Response(String code,T result){
        this.code=code;
        this.result=result;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }
}
