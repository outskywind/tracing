package com.dafy.skye.alertmanager.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class SendAlertsRequestDTO {

    private String notificationChannel;

    private long[] alertIds;

    private String[] receiverIds;

    private String[] ccIds;
}
