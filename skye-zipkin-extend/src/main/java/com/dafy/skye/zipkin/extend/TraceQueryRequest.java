package com.dafy.skye.zipkin.extend;

import com.dafy.skye.common.util.IntervalTimeUnit;

import java.util.List;
import java.util.Map;

/**
 * Created by Caedmon on 2017/6/16.
 */
public class TraceQueryRequest {
    public final long lookback;
    public final long endTs;
    public final  Integer interval;
    public final IntervalTimeUnit intervalUnit;
    public final List<String> serviceNames;
    public final List<String> spanNames;
    public final List<String> annotations;
    public final Map<String, String> binaryAnnotations;
    public final Long minDuration;
    public final Long maxDuration;
    public final int limit;

    private TraceQueryRequest(Builder builder) {
        lookback = builder.lookback;
        endTs = builder.endTs;
        interval = builder.interval;
        intervalUnit = builder.intervalUnit;
        serviceNames = builder.serviceNames;
        spanNames = builder.spanNames;
        annotations = builder.annotations;
        binaryAnnotations = builder.binaryAnnotations;
        minDuration = builder.minDuration;
        maxDuration = builder.maxDuration;
        limit = builder.limit;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static Builder newBuilder(TraceQueryRequest copy) {
        Builder builder = new Builder();
        builder.lookback = copy.lookback;
        builder.endTs = copy.endTs;
        builder.interval = copy.interval;
        builder.intervalUnit = copy.intervalUnit;
        builder.serviceNames = copy.serviceNames;
        builder.spanNames = copy.spanNames;
        builder.annotations = copy.annotations;
        builder.binaryAnnotations = copy.binaryAnnotations;
        builder.minDuration = copy.minDuration;
        builder.maxDuration = copy.maxDuration;
        builder.limit = copy.limit;
        return builder;
    }

    public static final class Builder {
        private long lookback=604800000;
        private long endTs=System.currentTimeMillis();
        private Integer interval=1;
        private IntervalTimeUnit intervalUnit=IntervalTimeUnit.DAY;
        private List<String> serviceNames;
        private List<String> spanNames;
        private List<String> annotations;
        private Map<String, String> binaryAnnotations;
        private Long minDuration;
        private Long maxDuration;
        private int limit;

        private Builder() {
        }

        public Builder lookback(long val) {
            lookback = val;
            return this;
        }

        public Builder endTs(long val) {
            endTs = val;
            return this;
        }

        public Builder interval(Integer val) {
            interval = val;
            return this;
        }

        public Builder intervalUnit(IntervalTimeUnit val) {
            intervalUnit = val;
            return this;
        }

        public Builder serviceNames(List<String> val) {
            serviceNames = val;
            return this;
        }

        public Builder spanNames(List<String> val) {
            spanNames = val;
            return this;
        }

        public Builder annotations(List<String> val) {
            annotations = val;
            return this;
        }

        public Builder binaryAnnotations(Map<String, String> val) {
            binaryAnnotations = val;
            return this;
        }

        public Builder minDuration(Long val) {
            minDuration = val;
            return this;
        }

        public Builder maxDuration(Long val) {
            maxDuration = val;
            return this;
        }

        public Builder limit(int val) {
            limit = val;
            return this;
        }

        public TraceQueryRequest build() {
            return new TraceQueryRequest(this);
        }
    }
}
