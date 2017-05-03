package com.dafy.skye.log.collector.storage.elasticsearch;

/**
 * Created by Caedmon on 2017/5/2.
 */
public class RestRequestException extends RuntimeException {
    private String method;
    private String endpoint;
    public RestRequestException(String method,String endpoint,Throwable throwable){
        super("["+method+"] "+endpoint,throwable);
        this.endpoint=endpoint;
        this.method=method;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }
}
