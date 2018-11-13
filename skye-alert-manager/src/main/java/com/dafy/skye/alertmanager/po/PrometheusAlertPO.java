package com.dafy.skye.alertmanager.po;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Setter
@Getter
@ToString
public class PrometheusAlertPO {
    private Long id;
    private String status;
    private String alertname;
    private String hostname;
    private String instance;
    private String job;
    private String receiverGroup;
    private String warningLevel;
    private String service;
    private String warningService;
    private String extraLabels;
    private String summary;
    private String grafanaLink;
    private String skyeLink;
    private String extraAnnotations;
    private Date startTime;
    private Integer version;
    private String groupLabels;
    private String receivers;
    private String cc;
    private Integer notificationChannel;
}
