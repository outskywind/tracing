package com.dafy.skye.zipkin.extend.dto;

import com.dafy.skye.common.query.QueryResult;

import java.util.List;

/**
 * Created by Caedmon on 2017/6/20.
 */
public class TraceMetricsResult extends QueryResult{
    private List<TraceMetrics> metrics;

    public List<TraceMetrics> getMetrics() {
        return metrics;
    }

    public void setMetrics(List<TraceMetrics> metrics) {
        this.metrics = metrics;
    }
    public  static class TraceMetrics {
        private long startTs;
        private long endTs;
        private long count;
        private long avgDuration;
        private long maxDuration;
        private long minDuration;

        private TraceMetrics(Builder builder) {
            setStartTs(builder.startTs);
            setEndTs(builder.endTs);
            setCount(builder.count);
            setAvgDuration(builder.avgDuration);
            setMaxDuration(builder.maxDuration);
            setMinDuration(builder.minDuration);
        }

        public static Builder newBuilder() {
            return new Builder();
        }

        public static Builder newBuilder(TraceMetrics copy) {
            Builder builder = new Builder();
            builder.startTs = copy.startTs;
            builder.endTs = copy.endTs;
            builder.count = copy.count;
            builder.avgDuration = copy.avgDuration;
            builder.maxDuration = copy.maxDuration;
            builder.minDuration = copy.minDuration;
            return builder;
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

        public long getCount() {
            return count;
        }

        public void setCount(long count) {
            this.count = count;
        }

        public long getAvgDuration() {
            return avgDuration;
        }

        public void setAvgDuration(long avgDuration) {
            this.avgDuration = avgDuration;
        }

        public long getMaxDuration() {
            return maxDuration;
        }

        public void setMaxDuration(long maxDuration) {
            this.maxDuration = maxDuration;
        }

        public long getMinDuration() {
            return minDuration;
        }

        public void setMinDuration(long minDuration) {
            this.minDuration = minDuration;
        }

        public static final class Builder {
            private long startTs;
            private long endTs;
            private long count;
            private long avgDuration;
            private long maxDuration;
            private long minDuration;

            private Builder() {
            }

            public Builder startTs(long val) {
                startTs = val;
                return this;
            }

            public Builder endTs(long val) {
                endTs = val;
                return this;
            }

            public Builder count(long val) {
                count = val;
                return this;
            }

            public Builder avgDuration(long val) {
                avgDuration = val;
                return this;
            }

            public Builder maxDuration(long val) {
                maxDuration = val;
                return this;
            }

            public Builder minDuration(long val) {
                minDuration = val;
                return this;
            }

            public TraceMetrics build() {
                return new TraceMetrics(this);
            }
        }
    }
}
