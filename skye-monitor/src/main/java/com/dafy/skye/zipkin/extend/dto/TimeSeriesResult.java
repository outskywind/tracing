package com.dafy.skye.zipkin.extend.dto;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by quanchengyun on 2017/7/3.
 */
public class TimeSeriesResult {
    /**
     * {"result":[{"name":"",series:[{start:1,end:2,avgDuration:12345.9}]}]}
     */
    //name 为折线标签
    private String name;
    private List<SeriesMetric> series=new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<SeriesMetric> getSeries() {
        return series;
    }

    public void setSeries(List<SeriesMetric> series) {
        this.series = series;
    }
}
