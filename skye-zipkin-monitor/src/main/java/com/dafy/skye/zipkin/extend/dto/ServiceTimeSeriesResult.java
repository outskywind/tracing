package com.dafy.skye.zipkin.extend.dto;

import com.dafy.skye.common.query.QueryResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by quanchengyun on 2017/7/3.
 */
public class ServiceTimeSeriesResult extends QueryResult {
    /**
     * {"data":[{"spanName":"",series:[{start:1,end:2,avgDuration:12345.9}]}]}
     */
    private List<ServiceInterfaceTimeSeries> data;

    public static class ServiceInterfaceTimeSeries{
        private String spanName;
        private List<SpanMetricsResult.InterfaceMetrics> series=new ArrayList<>();
        private long took;
        public String getSpanName() {
            return spanName;
        }

        public void setSpanName(String spanName) {
            this.spanName = spanName;
        }
        public List<SpanMetricsResult.InterfaceMetrics> getSeries() {
            return series;
        }

        public void setSeries(List<SpanMetricsResult.InterfaceMetrics> series) {
            this.series = series;
        }
        public long getTook() {
            return took;
        }

        public void setTook(long took) {
            this.took = took;
        }
    }

    public List<ServiceInterfaceTimeSeries> getData() {
        return data;
    }

    public void setData(List<ServiceInterfaceTimeSeries> data) {
        this.data = data;
    }

    public void addTook(long took){
        if(this.getTook()!=null){
            took = took+this.getTook().longValue();
        }
        this.setTook(new Long(took));
    }
}
