package com.dafy.skye.alertmanager.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class AlertExtraInfo {
    private String[] receiverIds;
    private String[] ccIds;
    private String groupName;
}
