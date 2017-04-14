package com.dafy.skye.klog.collector.consumer;

import com.dafy.skye.klog.collector.KafkaCollector;

import java.util.Properties;

/**
 * Created by Caedmon on 2017/4/14.
 */
public class ConsumerConfig {
    private String topic="skye-klog";
    private int partition;
    private int pullInterval=1000;
    private Properties properties;
    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public int getPartition() {
        return partition;
    }

    public void setPartition(int partition) {
        this.partition = partition;
    }

    public int getPullInterval() {
        return pullInterval;
    }

    public void setPullInterval(int pullInterval) {
        this.pullInterval = pullInterval;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }
}
