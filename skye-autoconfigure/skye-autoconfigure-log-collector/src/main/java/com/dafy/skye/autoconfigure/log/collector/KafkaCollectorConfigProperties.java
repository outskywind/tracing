package com.dafy.skye.autoconfigure.log.collector;

import com.dafy.skye.log.collector.kafka.KafkaCollectorConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * Created by Caedmon on 2017/4/25.
 */
@ConfigurationProperties("skye.log.collector.kafka")
public class KafkaCollectorConfigProperties {
    private String topic;
    private String groupId;
    private Integer partition;
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

    public KafkaCollectorConfig buildKafkaCollectorConfig(){
        KafkaCollectorConfig kafkaCollectorConfig=new KafkaCollectorConfig();
        kafkaCollectorConfig.setTopic(this.topic);
        kafkaCollectorConfig.setGroupId(this.groupId);
        kafkaCollectorConfig.setPartition(this.partition);
        kafkaCollectorConfig.setPollInterval(this.pollInterval);
        kafkaCollectorConfig.setProperties(this.properties);
        return kafkaCollectorConfig;
    }
}
