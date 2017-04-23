package com.dafy.skye.klog.collector;

/**
 * Created by Caedmon on 2017/4/16.
 */
public abstract class AbstractCollectorComponent implements CollectorComponent {
    protected CollectorPartitionConfig collectorPartitionConfig;

    @Override
    public void setCollectorPartitionConfig(CollectorPartitionConfig collectorPartitionConfig) {
        this.collectorPartitionConfig = collectorPartitionConfig;
    }

    @Override
    public CollectorPartitionConfig getCollectorPartitionConfig() {
        return this.collectorPartitionConfig;
    }
}
