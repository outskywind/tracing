package com.dafy.skye.log.server.storage.query;

import com.dafy.skye.log.server.storage.entity.SkyeLogEntity;

import java.util.List;
import java.util.Map;

/**
 * Created by Caedmon on 2017/6/7.
 */
public class LogQueryResult {
    private Integer took;
    private boolean success;
    private String error;
    private List<SkyeLogEntity> content;
    private Integer total;
    private Map<String,Object> extra;
    public static Builder newBuilder(){
        return new Builder();
    }

    private LogQueryResult(Builder builder) {
        setTook(builder.took);
        setSuccess(builder.success);
        setError(builder.error);
        setContent(builder.content);
        setTotal(builder.total);
        setExtra(builder.extra);
    }

    public Integer getTook() {
        return took;
    }

    public void setTook(Integer took) {
        this.took = took;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
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

    public Map<String, Object> getExtra() {
        return extra;
    }

    public void setExtra(Map<String, Object> extra) {
        this.extra = extra;
    }

    public static final class Builder {
        private Integer took;
        private boolean success;
        private String error;
        private List<SkyeLogEntity> content;
        private Integer total;
        private Map<String, Object> extra;

        public Builder() {
        }

        public Builder took(Integer val) {
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

        public Builder content(List<SkyeLogEntity> val) {
            content = val;
            return this;
        }

        public Builder total(Integer val) {
            total = val;
            return this;
        }

        public Builder extra(Map<String, Object> val) {
            extra = val;
            return this;
        }

        public LogQueryResult build() {
            return new LogQueryResult(this);
        }
    }

}
