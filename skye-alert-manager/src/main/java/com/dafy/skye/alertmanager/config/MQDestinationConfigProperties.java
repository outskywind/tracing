package com.dafy.skye.alertmanager.config;

import lombok.Setter;
import org.apache.activemq.command.ActiveMQTopic;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.jms.Topic;

@Setter
@Configuration
@ConfigurationProperties("alert.mq")
public class MQDestinationConfigProperties {

    private String topic;

    @Bean(name = "alertTopic")
    public Topic alertTopic() {
        return new ActiveMQTopic(topic);
    }

}
