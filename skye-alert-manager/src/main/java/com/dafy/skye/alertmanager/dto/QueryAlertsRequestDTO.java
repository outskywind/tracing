package com.dafy.skye.alertmanager.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class QueryAlertsRequestDTO {
    private String[] services;
    private long startTime;
    private long endTime;
    private int pageNo;
    private int pageSize;
}
