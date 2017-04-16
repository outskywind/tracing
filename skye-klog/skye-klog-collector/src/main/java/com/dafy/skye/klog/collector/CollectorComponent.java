package com.dafy.skye.klog.collector;

/**
 * Created by Caedmon on 2017/4/16.
 */
public interface CollectorComponent {
    void start();

    void stop();

    void setCollectorConfig(CollectorConfig collectorConfig);

    CollectorConfig getCollectorConfig();
}
