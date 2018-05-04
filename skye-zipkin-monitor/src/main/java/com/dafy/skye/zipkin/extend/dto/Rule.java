package com.dafy.skye.zipkin.extend.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Created by quanchengyun on 2018/4/12.
 */
public class Rule {

    private int id;
    private String type;
    private String dimension;
    private String condition;
    private Threshold threshold;

    private String service;
    private String spanName;
    @JsonIgnore
    private String thresholdStr;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getSpanName() {
        return spanName;
    }

    public void setSpanName(String spanName) {
        this.spanName = spanName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDimension() {
        return dimension;
    }

    public void setDimension(String dimension) {
        this.dimension = dimension;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public Threshold getThreshold() {
        return threshold;
    }

    public void setThreshold(Threshold threshold) {
        this.threshold = threshold;
    }

    public String getThresholdStr() {
        return thresholdStr;
    }

    public void setThresholdStr(String thresholdStr) {
        this.thresholdStr = thresholdStr;
    }
}
