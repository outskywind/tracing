package com.dafy.skye.zipkin.extend.dto;

import com.dafy.skye.common.query.QueryResult;

import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Created by Caedmon on 2017/6/21.
 */
public class SpanMetricsResult extends QueryResult{
    private TreeSet<SpanMetrics> metrics =new TreeSet<>();

    public TreeSet<SpanMetrics> getMetrics() {
        return metrics;
    }

    public void setMetrics(TreeSet<SpanMetrics> metrics) {
        this.metrics = metrics;
    }

    public static class SpanMetrics implements Comparable<SpanMetrics>{
        private String spanName;
        private double avgDuration;
        private double minDuration;
        private double maxDuration;
        private double sumDuration;
        private long count;
        private long startTs;
        private long endTs;

        private SpanMetrics(Builder builder) {
            setSpanName(builder.spanName);
            setAvgDuration(builder.avgDuration);
            setMinDuration(builder.minDuration);
            setMaxDuration(builder.maxDuration);
            setSumDuration(builder.sumDuration);
            setCount(builder.count);
            startTs = builder.startTs;
            endTs = builder.endTs;
        }

        @Override
        public int compareTo(SpanMetrics o) {
            double result=o.avgDuration-this.avgDuration;
            if(result==0){
                result=o.sumDuration-this.sumDuration;
                if(result==0){
                    result=o.count-this.count;
                }
            }
            if(result<0){
                return -1;
            }else if(result==0){
                return 0;
            }else{
                return 1;
            }
        }

        public static Builder newBuilder() {
            return new Builder();
        }

        public static Builder newBuilder(SpanMetrics copy) {
            Builder builder = new Builder();
            builder.spanName = copy.spanName;
            builder.avgDuration = copy.avgDuration;
            builder.minDuration = copy.minDuration;
            builder.maxDuration = copy.maxDuration;
            builder.sumDuration = copy.sumDuration;
            builder.count = copy.count;
            builder.startTs = copy.startTs;
            builder.endTs = copy.endTs;
            return builder;
        }

        public String getSpanName() {
            return spanName;
        }

        public void setSpanName(String spanName) {
            this.spanName = spanName;
        }

        public double getAvgDuration() {
            return avgDuration;
        }

        public void setAvgDuration(double avgDuration) {
            this.avgDuration = avgDuration;
        }

        public double getMinDuration() {
            return minDuration;
        }

        public void setMinDuration(double minDuration) {
            this.minDuration = minDuration;
        }

        public double getMaxDuration() {
            return maxDuration;
        }

        public void setMaxDuration(double maxDuration) {
            this.maxDuration = maxDuration;
        }

        public double getSumDuration() {
            return sumDuration;
        }

        public void setSumDuration(double sumDuration) {
            this.sumDuration = sumDuration;
        }

        public long getCount() {
            return count;
        }

        public void setCount(long count) {
            this.count = count;
        }

        public static final class Builder {
            private String spanName;
            private double avgDuration;
            private double minDuration;
            private double maxDuration;
            private double sumDuration;
            private long count;
            private long startTs;
            private long endTs;

            private Builder() {
            }

            public Builder spanName(String val) {
                spanName = val;
                return this;
            }

            public Builder avgDuration(double val) {
                avgDuration = val;
                return this;
            }

            public Builder minDuration(double val) {
                minDuration = val;
                return this;
            }

            public Builder maxDuration(double val) {
                maxDuration = val;
                return this;
            }

            public Builder sumDuration(double val) {
                sumDuration = val;
                return this;
            }

            public Builder count(long val) {
                count = val;
                return this;
            }

            public Builder startTs(long val) {
                startTs = val;
                return this;
            }

            public Builder endTs(long val) {
                endTs = val;
                return this;
            }

            public SpanMetrics build() {
                return new SpanMetrics(this);
            }
        }
    }
}
