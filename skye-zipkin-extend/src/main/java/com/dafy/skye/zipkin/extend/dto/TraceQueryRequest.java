package com.dafy.skye.zipkin.extend.dto;

import com.dafy.skye.common.util.IntervalTimeUnit;

import java.util.*;

/**
 * Created by Caedmon on 2017/6/16.
 */
public class TraceQueryRequest extends TimeBaseQueryRequest{
    public  Integer interval;
    public IntervalTimeUnit intervalUnit;
    public List<String> services;
    public List<String> spans;
    public List<String> annotations;
    public Map<String, String> binaryAnnotations;
    public Long minDuration;
    public Long maxDuration;
    public int limit;
    public String sortField="timestamp_millis";
    public String sortOrder="desc";

    public TraceQueryRequest() {
    }

    private TraceQueryRequest(Builder builder) {
        setEndTs(builder.endTs);
        setLookback(builder.lookback);
        setInterval(builder.interval);
        setIntervalUnit(builder.intervalUnit);
        setServices(builder.services);
        setSpans(builder.spans);
        setAnnotations(builder.annotations);
        setBinaryAnnotations(builder.binaryAnnotations);
        setMinDuration(builder.minDuration);
        setMaxDuration(builder.maxDuration);
        setLimit(builder.limit);
        setSortField(builder.sortField);
        setSortOrder(builder.sortOrder);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static Builder newBuilder(TraceQueryRequest copy) {
        Builder builder = new Builder();
        builder.endTs = copy.endTs;
        builder.lookback = copy.lookback;
        builder.interval = copy.interval;
        builder.intervalUnit = copy.intervalUnit;
        builder.services = copy.services;
        builder.spans = copy.spans;
        builder.annotations = copy.annotations;
        builder.binaryAnnotations = copy.binaryAnnotations;
        builder.minDuration = copy.minDuration;
        builder.maxDuration = copy.maxDuration;
        builder.limit = copy.limit;
        builder.sortField = copy.sortField;
        builder.sortOrder = copy.sortOrder;
        return builder;
    }

    public Integer getInterval() {
        return interval;
    }

    public void setInterval(Integer interval) {
        this.interval = interval;
    }

    public IntervalTimeUnit getIntervalUnit() {
        return intervalUnit;
    }

    public void setIntervalUnit(IntervalTimeUnit intervalUnit) {
        this.intervalUnit = intervalUnit;
    }

    public List<String> getServices() {
        return services;
    }

    public void setServices(List<String> services) {
        this.services = services;
    }

    public List<String> getSpans() {
        return spans;
    }

    public void setSpans(List<String> spans) {
        this.spans = spans;
    }

    public List<String> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(List<String> annotations) {
        this.annotations = annotations;
    }

    public Map<String, String> getBinaryAnnotations() {
        return binaryAnnotations;
    }

    public void setBinaryAnnotations(Map<String, String> binaryAnnotations) {
        this.binaryAnnotations = binaryAnnotations;
    }

    public Long getMinDuration() {
        return minDuration;
    }

    public void setMinDuration(Long minDuration) {
        this.minDuration = minDuration;
    }

    public Long getMaxDuration() {
        return maxDuration;
    }

    public void setMaxDuration(Long maxDuration) {
        this.maxDuration = maxDuration;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public String getSortField() {
        return sortField;
    }

    public void setSortField(String sortField) {
        this.sortField = sortField;
    }

    public String getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }

    public static final class Builder {
        private Long endTs;
        private Long lookback;
        private Integer interval;
        private IntervalTimeUnit intervalUnit;
        private List<String> services;
        private List<String> spans;
        private List<String> annotations;
        private Map<String, String> binaryAnnotations;
        private Long minDuration;
        private Long maxDuration;
        private int limit;
        private String sortField;
        private String sortOrder;

        private Builder() {
        }

        public Builder endTs(Long val) {
            endTs = val;
            return this;
        }

        public Builder lookback(Long val) {
            lookback = val;
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

        public Builder services(List<String> val) {
            services = val;
            return this;
        }

        public Builder spans(List<String> val) {
            spans = val;
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

        public Builder sortField(String val) {
            sortField = val;
            return this;
        }

        public Builder sortOrder(String val) {
            sortOrder = val;
            return this;
        }

        public TraceQueryRequest build() {
            return new TraceQueryRequest(this);
        }
    }
}
