package com.dafy.skye.storm.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Created by quanchengyun on 2017/8/24.
 */
@ConfigurationProperties("storm.kafka")
public class KafkaConfigProperties {

    private String bootstrapServers;

    private List<String> topics;

    private String consumerGroup;

    private int maxPollSize;

    public String getBootstrapServers() {
        return bootstrapServers;
    }

    public void setBootstrapServers(String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }

    public List<String> getTopics() {
        return topics;
    }

    public void setTopics(List<String> topics) {
        this.topics = topics;
    }

    public String getConsumerGroup() {
        return consumerGroup;
    }

    public void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }

    public int getMaxPollSize() {
        return maxPollSize;
    }

    public void setMaxPollSize(int maxPollSize) {
        this.maxPollSize = maxPollSize;
    }
}
