package com.dafy.skye.klog.collector;

/**
 * Created by Caedmon on 2017/4/16.
 */
public abstract class AbstractCollectorComponent implements CollectorComponent {
    protected CollectorConfig collectorConfig;

    @Override
    public void setCollectorConfig(CollectorConfig collectorConfig) {
        this.collectorConfig=collectorConfig;
    }

    @Override
    public CollectorConfig getCollectorConfig() {
        return this.collectorConfig;
    }
}
