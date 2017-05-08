package com.dafy.skye.log.collector.metrics;

/**
 * Created by Caedmon on 2017/4/25.
 */
public interface CollectorMetrics {
    void incrementBytes(int quantity);

    void incrementMessages(int quantity);

    void incrementMessageDropped(int quantity);
}
