package com.dafy.skye.zipkin.extend.dto;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by Caedmon on 2017/6/24.
 */
public class TimeBaseQueryRequest {
    public Long endTs;
    public Long lookback;

    public Long getEndTs() {
        return endTs;
    }

    public void setEndTs(Long endTs) {
        this.endTs = endTs;
    }

    public Long getLookback() {
        return lookback;
    }

    public void setLookback(Long lookback) {
        this.lookback = lookback;
    }
}
