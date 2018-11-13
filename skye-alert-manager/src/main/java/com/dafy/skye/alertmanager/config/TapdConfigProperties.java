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
@ConfigurationProperties("tapd")
public class TapdConfigProperties {

    private String workspaceId;

    private String reporter;

}
