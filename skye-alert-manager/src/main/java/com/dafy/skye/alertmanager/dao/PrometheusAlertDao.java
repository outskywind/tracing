package com.dafy.skye.alertmanager.dao;

import com.dafy.skye.alertmanager.constant.NotificationChannel;
import com.dafy.skye.alertmanager.po.PrometheusAlertPO;

import java.util.List;

public interface PrometheusAlertDao {

    boolean addAlert(PrometheusAlertPO alert);

    List<PrometheusAlertPO> getAlerts(String[] services, String startTime, String endTime, int limit);

    List<PrometheusAlertPO> getAlertsByIds(long[] ids);

    PrometheusAlertPO getAlertById(long id);

    boolean updateAlerts(long[] ids, String receivers, String cc, NotificationChannel notificationChannel);
}
