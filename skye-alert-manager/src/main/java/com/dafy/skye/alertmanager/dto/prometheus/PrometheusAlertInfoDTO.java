package com.dafy.skye.alertmanager.dto.prometheus;

import lombok.Builder;

@Builder(builderClassName = "Builder")
public class PrometheusAlertInfoDTO {
    private String status;
    private String alertname;
    private String hostname;
    private String instance;
    private String warningLevel;
    private String warningService;
    private String summary;
    private String startTime;
}
