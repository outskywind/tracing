package com.dafy.skye.common.query;

import java.util.Map;

/**
 * Created by Caedmon on 2017/6/20.
 */
public class QueryResult {
    protected Long took;
    protected boolean success;
    protected String error;
    protected Map<String,Object> extra;

    public Long getTook() {
        return took;
    }

    public void setTook(Long took) {
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
