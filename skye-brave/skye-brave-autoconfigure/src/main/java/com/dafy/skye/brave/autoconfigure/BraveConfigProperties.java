package com.dafy.skye.brave.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Created by Caedmon on 2017/6/27.
 */
@ConfigurationProperties("skye.brave")
public class BraveConfigProperties {
    private String serviceName;
    private Float samplerRate;
    private String kafkaServers;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public Float getSamplerRate() {
        return samplerRate;
    }

    public void setSamplerRate(Float samplerRate) {
        this.samplerRate = samplerRate;
    }

    public String getKafkaServers() {
        return kafkaServers;
    }

    public void setKafkaServers(String kafkaServers) {
        this.kafkaServers = kafkaServers;
    }
}
