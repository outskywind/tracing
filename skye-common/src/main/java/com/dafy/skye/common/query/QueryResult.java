package com.dafy.skye.common.query;

import java.util.Map;

/**
 * Created by Caedmon on 2017/6/20.
 */
public class QueryResult {
    protected Integer took;
    protected boolean success;
    protected String error;
    protected Map<String,Object> extra;
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

    public Map<String, Object> getExtra() {
        return extra;
    }

    public void setExtra(Map<String, Object> extra) {
        this.extra = extra;
    }
}
