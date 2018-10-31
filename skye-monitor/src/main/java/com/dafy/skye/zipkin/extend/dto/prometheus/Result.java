package com.dafy.skye.zipkin.extend.dto.prometheus;

import java.util.List;
import java.util.Map;

/**
 * Created by quanchengyun on 2018/10/21.
 */
public class Result {

    private Map<String,String> metric;

    private List<Object> value;

    private List<List<Object>> values;

    public Map<String, String> getMetric() {
        return metric;
    }

    public void setMetric(Map<String, String> metric) {
        this.metric = metric;
    }

    public List<Object> getValue() {
        return value;
    }

    public void setValue(List<Object> value) {
        this.value = value;
    }

    public List<List<Object>> getValues() {
        return values;
    }

    public void setValues(List<List<Object>> values) {
        this.values = values;
    }
}
