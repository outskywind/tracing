package com.dafy.skye.zipkin.extend.dto;

import com.dafy.skye.common.query.QueryResult;

import java.util.List;

/**
 * Created by quanchengyun on 2017/7/3.
 */
public class SpanTimeSeriesResult extends QueryResult {
    /**
     * {"seriesResult":[{"spanName":"",series:[{start:1,end:2,avgDuration:12345.9}]}]}
     */
    private List<SpanTimeSeries> seriesResult;

    public static class SpanTimeSeries{
        private String spanName;
        private List<SpanMetricsResult.SpanMetrics> series;
        private long took;
        public String getSpanName() {
            return spanName;
        }

        public void setSpanName(String spanName) {
            this.spanName = spanName;
        }

        public List<SpanMetricsResult.SpanMetrics> getSeries() {
            return series;
        }

        public void setSeries(List<SpanMetricsResult.SpanMetrics> series) {
            this.series = series;
        }
        public long getTook() {
            return took;
        }

        public void setTook(long took) {
            this.took = took;
        }
    }

    public List<SpanTimeSeries> getSeriesResult() {
        return seriesResult;
    }

    public void setSeriesResult(List<SpanTimeSeries> seriesResult) {
        this.seriesResult = seriesResult;
    }

    public void addTook(long took){
        if(this.getTook()!=null){
            took = took+this.getTook().longValue();
        }
        this.setTook(new Long(took));
    }
}
