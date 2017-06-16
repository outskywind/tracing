package com.dafy.skye.zipkin.extend;

import com.dafy.skye.common.util.IntervalTimeUnit;

import java.util.List;
import java.util.Map;

/**
 * Created by Caedmon on 2017/6/16.
 */
public class TraceQueryRequest {
    public final Long startTs;
    public final  Integer interval;
    public final IntervalTimeUnit intervalUnit;
    public final String serviceName;
    public final String spanName;
    public final List<String> annotations;
    public final Map<String, String> binaryAnnotations;
    public final Long minDuration;
    public final Long maxDuration;
    public final long endTs;
    public final int limit;
    private TraceQueryRequest(Builder builder) {
        startTs = builder.startTs;
        interval = builder.interval;
        intervalUnit = builder.intervalUnit;
        serviceName = builder.serviceName;
        spanName = builder.spanName;
        annotations = builder.annotations;
        binaryAnnotations = builder.binaryAnnotations;
        minDuration = builder.minDuration;
        maxDuration = builder.maxDuration;
        endTs = builder.endTs;
        limit = builder.limit;
    }

    public static final class Builder {
        private final Long startTs;
        private final Integer interval;
        private final IntervalTimeUnit intervalUnit;
        private final String serviceName;
        private final String spanName;
        private final List<String> annotations;
        private final Map<String, String> binaryAnnotations;
        private final Long minDuration;
        private final Long maxDuration;
        private final long endTs;
        private final int limit;

        public Builder(Long startTs, Integer interval, IntervalTimeUnit intervalUnit, String serviceName, String spanName, List<String> annotations, Map<String, String> binaryAnnotations, Long minDuration, Long maxDuration, long endTs, int limit) {
            this.startTs = startTs;
            this.interval = interval;
            this.intervalUnit = intervalUnit;
            this.serviceName = serviceName;
            this.spanName = spanName;
            this.annotations = annotations;
            this.binaryAnnotations = binaryAnnotations;
            this.minDuration = minDuration;
            this.maxDuration = maxDuration;
            this.endTs = endTs;
            this.limit = limit;
        }

        public TraceQueryRequest build() {
            return new TraceQueryRequest(this);
        }
    }
}
