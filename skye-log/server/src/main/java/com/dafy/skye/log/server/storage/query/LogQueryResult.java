package com.dafy.skye.log.server.storage.query;

import com.dafy.skye.common.query.QueryResult;
import com.dafy.skye.log.server.storage.entity.SkyeLogEntity;

import java.util.List;
import java.util.Map;

/**
 * Created by Caedmon on 2017/6/7.
 */
public class LogQueryResult extends QueryResult{
    private List<SkyeLogEntity> content;
    private Integer total;

    private LogQueryResult(Builder builder) {
        setTook(builder.took);
        setSuccess(builder.success);
        setError(builder.error);
        setExtra(builder.extra);
        setContent(builder.content);
        setTotal(builder.total);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static Builder newBuilder(LogQueryResult copy) {
        Builder builder = new Builder();
        builder.took = copy.took;
        builder.success = copy.success;
        builder.error = copy.error;
        builder.extra = copy.extra;
        builder.content = copy.content;
        builder.total = copy.total;
        return builder;
    }

    public List<SkyeLogEntity> getContent() {
        return content;
    }

    public void setContent(List<SkyeLogEntity> content) {
        this.content = content;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    @Override
    public String toString() {
        return "LogQueryResult{" +
                "took=" + took +
                ", success=" + success +
                ", error='" + error + '\'' +
                ", extra=" + extra +
                ", content=" + content +
                ", total=" + total +
                '}';
    }

    public static final class Builder {
        private Long took;
        private boolean success;
        private String error;
        private Map<String, Object> extra;
        private List<SkyeLogEntity> content;
        private Integer total;

        private Builder() {
        }

        public Builder took(Long val) {
            took = val;
            return this;
        }

        public Builder success(boolean val) {
            success = val;
            return this;
        }

        public Builder error(String val) {
            error = val;
            return this;
        }

        public Builder extra(Map<String, Object> val) {
            extra = val;
            return this;
        }

        public Builder content(List<SkyeLogEntity> val) {
            content = val;
            return this;
        }

        public Builder total(Integer val) {
            total = val;
            return this;
        }

        public LogQueryResult build() {
            return new LogQueryResult(this);
        }
    }
}
