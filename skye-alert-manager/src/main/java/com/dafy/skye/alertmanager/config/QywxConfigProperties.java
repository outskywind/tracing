package com.dafy.skye.alertmanager.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@ToString
@Configuration
@ConfigurationProperties("qywx")
public class QywxConfigProperties {

    private String corpId;
    private String secret;
    private Integer agentId;

}
