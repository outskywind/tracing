package com.dafy.skye.klog.collector.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Created by Caedmon on 2017/4/24.
 */
@ConfigurationProperties(prefix = "skye.klog.collector")
public class KLogCollectorConfigProperties {
    private int parallel;
    private String topic;
    private String groupId;
    public int getParallel() {
        return parallel;
    }

    public void setParallel(int parallel) {
        this.parallel = parallel;
    }

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
}
