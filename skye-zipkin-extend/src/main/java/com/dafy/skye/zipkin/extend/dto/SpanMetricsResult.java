package com.dafy.skye.zipkin.extend.dto;

import com.dafy.skye.common.query.QueryResult;

import java.util.TreeSet;

/**
 * Created by Caedmon on 2017/6/21.
 */
public class SpanMetricsResult extends QueryResult{
    private TreeSet<SpanMetrics> data =new TreeSet<>();

    public TreeSet<SpanMetrics> getData() {
        return data;
    }

    public void setData(TreeSet<SpanMetrics> data) {
        this.data = data;
    }

    public static class SpanMetrics implements Comparable<SpanMetrics>{
        private String spanName;
        private long avgDuration;
        private long minDuration;
        private long maxDuration;
        private long sumDuration;
        private long count;
        private long startTs;
        private long endTs;
        public SpanMetrics(){

        }

        private SpanMetrics(Builder builder) {
            setSpanName(builder.spanName);
            setAvgDuration(builder.avgDuration);
            setMinDuration(builder.minDuration);
            setMaxDuration(builder.maxDuration);
            setSumDuration(builder.sumDuration);
            setCount(builder.count);
            setStartTs(builder.startTs);
            setEndTs(builder.endTs);
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

        public long getAvgDuration() {
            return avgDuration;
        }

        public void setAvgDuration(long avgDuration) {
            this.avgDuration = avgDuration;
        }

        public long getMinDuration() {
            return minDuration;
        }

        public void setMinDuration(long minDuration) {
            this.minDuration = minDuration;
        }

        public long getMaxDuration() {
            return maxDuration;
        }

        public void setMaxDuration(long maxDuration) {
            this.maxDuration = maxDuration;
        }

        public long getSumDuration() {
            return sumDuration;
        }

        public void setSumDuration(long sumDuration) {
            this.sumDuration = sumDuration;
        }

        public long getCount() {
            return count;
        }

        public void setCount(long count) {
            this.count = count;
        }

        public long getStartTs() {
            return startTs;
        }

        public void setStartTs(long startTs) {
            this.startTs = startTs;
        }

        public long getEndTs() {
            return endTs;
        }

        public void setEndTs(long endTs) {
            this.endTs = endTs;
        }

        @Override
        public int compareTo(SpanMetrics o) {
            long result=o.avgDuration-this.avgDuration;
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

        public static final class Builder {
            private String spanName;
            private long avgDuration;
            private long minDuration;
            private long maxDuration;
            private long sumDuration;
            private long count;
            private long startTs;
            private long endTs;

            private Builder() {
            }

            public Builder spanName(String val) {
                spanName = val;
                return this;
            }

            public Builder avgDuration(long val) {
                avgDuration = val;
                return this;
            }

            public Builder minDuration(long val) {
                minDuration = val;
                return this;
            }

            public Builder maxDuration(long val) {
                maxDuration = val;
                return this;
            }

            public Builder sumDuration(long val) {
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
