package com.dafy.skye.druid;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Created by quanchengyun on 2017/9/19.
 */
@ConfigurationProperties("druid.broker")
public class DruidConfigurationProperties {

    private String banlanceStrategy;

    private int maxTotalSeries=100;

    public String getBanlanceStrategy() {
        return banlanceStrategy;
    }

    public void setBanlanceStrategy(String banlanceStrategy) {
        this.banlanceStrategy = banlanceStrategy;
    }

    public int getMaxTotalSeries() {
        return maxTotalSeries;
    }

    public void setMaxTotalSeries(int maxTotalSeries) {
        this.maxTotalSeries = maxTotalSeries;
    }
}
