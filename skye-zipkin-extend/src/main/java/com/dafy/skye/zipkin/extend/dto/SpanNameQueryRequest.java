package com.dafy.skye.zipkin.extend.dto;

import java.util.List;

/**
 * Created by Caedmon on 2017/6/24.
 */
public class SpanNameQueryRequest extends TimeBaseQueryRequest{
    public List<String> services;

    public List<String> getServices() {
        return services;
    }

    public void setServices(List<String> services) {
        this.services = services;
    }

}
