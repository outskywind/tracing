package com.dafy.skye.zipkin.extend;

/**
 * Created by Caedmon on 2017/6/19.
 */
public class TraceIntervalStats {
    private final long startTs;
    private final long endTs;
    private final long count;
    private final double avgDuration;
    private final  double maxDuration;
    private final double minDuration;

    private TraceIntervalStats(Builder builder) {
        startTs = builder.startTs;
        endTs = builder.endTs;
        count = builder.count;
        avgDuration = builder.avgDuration;
        maxDuration = builder.maxDuration;
        minDuration = builder.minDuration;
    }

    public long getStartTs() {
        return startTs;
    }

    public long getEndTs() {
        return endTs;
    }

    public long getCount() {
        return count;
    }

    public double getAvgDuration() {
        return avgDuration;
    }

    public double getMaxDuration() {
        return maxDuration;
    }

    public double getMinDuration() {
        return minDuration;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static Builder newBuilder(TraceIntervalStats copy) {
        Builder builder = new Builder();
        builder.startTs = copy.startTs;
        builder.endTs = copy.endTs;
        builder.count = copy.count;
        builder.avgDuration = copy.avgDuration;
        builder.maxDuration = copy.maxDuration;
        builder.minDuration = copy.minDuration;
        return builder;
    }
    public static final class Builder {
        private long startTs;
        private long endTs;
        private long count;
        private double avgDuration;
        private double maxDuration;
        private double minDuration;

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

        public Builder avgDuration(double val) {
            avgDuration = val;
            return this;
        }

        public Builder maxDuration(double val) {
            maxDuration = val;
            return this;
        }

        public Builder minDuration(double val) {
            minDuration = val;
            return this;
        }

        public TraceIntervalStats build() {
            return new TraceIntervalStats(this);
        }
    }
}
