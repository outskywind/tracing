package com.dafy.skye.log.server.autoconfig;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * Created by Caedmon on 2017/4/25.
 */
@ConfigurationProperties("skye.log.server.kafka")
public class KafkaCollectorConfigProperties {
    private String topic;
    private String groupId;
    private Integer parallel=2;
    private Long pollInterval=1000L;
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

    public Integer getParallel() {
        return parallel;
    }

    public void setParallel(Integer parallel) {
        this.parallel = parallel;
    }

    public void setPollInterval(Long pollInterval) {
        this.pollInterval = pollInterval;
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

}
