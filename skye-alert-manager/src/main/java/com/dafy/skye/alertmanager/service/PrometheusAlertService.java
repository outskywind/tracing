package com.dafy.skye.alertmanager.service;

import com.dafy.skye.alertmanager.dto.SendAlertsRequestDTO;
import com.dafy.skye.alertmanager.dto.prometheus.PrometheusAlertInfoDTO;
import com.dafy.skye.alertmanager.dto.prometheus.PrometheusWebHookDTO;
import com.dafy.skye.alertmanager.dto.QueryAlertsRequestDTO;

import java.util.List;

public interface PrometheusAlertService {

    void saveAlerts(PrometheusWebHookDTO dto);

    List<PrometheusAlertInfoDTO> getAlerts(QueryAlertsRequestDTO dto);

    void sendAlerts(SendAlertsRequestDTO dto);
}
