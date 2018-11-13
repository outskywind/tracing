package com.dafy.skye.alertmanager.notifier;

import com.dafy.skye.alertmanager.constant.NotificationChannel;
import com.dafy.skye.alertmanager.dto.AlertExtraInfo;
import com.dafy.skye.alertmanager.po.PrometheusAlertPO;

import java.util.List;

public interface Notifier {

    void sendAlerts(List<PrometheusAlertPO> alerts, AlertExtraInfo extraInfo);

    NotificationChannel getNotificationChannel();

}
