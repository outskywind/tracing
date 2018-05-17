package com.dafy.kafka;

import java.util.Properties;

/**
 * Created by quanchengyun on 2018/5/7.
 */
public class KafkaConfigurationProperties {

    private String topic;

    private Properties properties;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }
}
