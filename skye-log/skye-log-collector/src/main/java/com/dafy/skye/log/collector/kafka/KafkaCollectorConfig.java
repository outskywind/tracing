package com.dafy.skye.log.collector.kafka;

import java.util.Map;

/**
 * Created by Caedmon on 2017/4/25.
 */
public class KafkaCollectorConfig {
    private String topic;
    private String groupId;
    private int partition;
    private long pollInterval;
    private Map<String,String> properties;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public int getPartition() {
        return partition;
    }

    public void setPartition(int partition) {
        this.partition = partition;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public long getPollInterval() {
        return pollInterval;
    }

    public void setPollInterval(long pollInterval) {
        this.pollInterval = pollInterval;
    }

    @Override
    public String toString() {
        return "KafkaCollectorConfig{" +
                "topic='" + topic + '\'' +
                ", groupId='" + groupId + '\'' +
                ", partition=" + partition +
                ", pollInterval=" + pollInterval +
                ", properties=" + properties +
                '}';
    }
}
