package com.dafy.skye.autoconfigure;

/**
 * Created by Caedmon on 2017/6/27.
 */
public class BraveConfigProperties {
    private String serviceName;
    private Float samplerRate;
    private String kafkaServers;
    private String consulServer;
    private String consulCheckInterval = "5s";

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

    public String getConsulServer() {
        return consulServer;
    }

    public void setConsulServer(String consulServer) {
        this.consulServer = consulServer;
    }

    public String getConsulCheckInterval() {
        return consulCheckInterval;
    }

    public void setConsulCheckInterval(String consulCheckInterval) {
        this.consulCheckInterval = consulCheckInterval;
    }
}
