package com.dafy.skye.alertmanager.service;

import com.dafy.skye.alertmanager.dto.SendAlertsRequestDTO;
import com.dafy.skye.alertmanager.dto.prometheus.PrometheusAlertInfoDTO;
import com.dafy.skye.alertmanager.dto.prometheus.PrometheusWebHookDTO;
import com.dafy.skye.alertmanager.dto.QueryAlertsRequestDTO;
import com.dafy.skye.alertmanager.po.PrometheusAlertPO;

import java.util.List;

public interface PrometheusAlertService {

    List<PrometheusAlertPO> saveAlerts(PrometheusWebHookDTO dto);

    List<PrometheusAlertInfoDTO> getAlerts(QueryAlertsRequestDTO dto);

    void sendAlerts(SendAlertsRequestDTO dto);

    void pushAlerts(List<PrometheusAlertPO> alerts);
}
