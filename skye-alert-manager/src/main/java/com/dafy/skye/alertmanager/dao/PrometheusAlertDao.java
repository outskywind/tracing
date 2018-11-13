package com.dafy.skye.alertmanager.dao;

import com.dafy.skye.alertmanager.po.PrometheusAlertPO;

import java.util.List;

public interface PrometheusAlertDao {

    int addAlert(PrometheusAlertPO alert);

    List<PrometheusAlertPO> getAlerts(String[] services, String startTime, String endTime, int limit);

    List<PrometheusAlertPO> getAlertsByIds(long[] ids);

    PrometheusAlertPO getAlertById(long id);

}
