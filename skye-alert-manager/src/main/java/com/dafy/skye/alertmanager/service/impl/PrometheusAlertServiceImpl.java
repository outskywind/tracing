package com.dafy.skye.alertmanager.service.impl;

import com.alibaba.fastjson.JSON;
import com.dafy.skye.alertmanager.constant.NotificationChannel;
import com.dafy.skye.alertmanager.constant.PrometheusAnnotation;
import com.dafy.skye.alertmanager.constant.PrometheusLabel;
import com.dafy.skye.alertmanager.dao.PrometheusAlertDao;
import com.dafy.skye.alertmanager.dto.AlertExtraInfo;
import com.dafy.skye.alertmanager.dto.QueryAlertsRequestDTO;
import com.dafy.skye.alertmanager.dto.SendAlertsRequestDTO;
import com.dafy.skye.alertmanager.dto.prometheus.PrometheusAlertInfoDTO;
import com.dafy.skye.alertmanager.dto.prometheus.PrometheusWebHookDTO;
import com.dafy.skye.alertmanager.notifier.Notifier;
import com.dafy.skye.alertmanager.po.PrometheusAlertPO;
import com.dafy.skye.alertmanager.service.PrometheusAlertService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import javax.jms.Topic;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.dafy.skye.alertmanager.dto.prometheus.PrometheusWebHookDTO.Alert;

@Slf4j
@Service
public class PrometheusAlertServiceImpl implements PrometheusAlertService {

    private final static String DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    private final PrometheusAlertDao alertDao;
    private final Map<NotificationChannel, Notifier> notifierMapping;
    private final JmsTemplate jmsTemplate;
    private final Topic alertTopic;

    @Autowired
    public PrometheusAlertServiceImpl(PrometheusAlertDao alertDao,
                                      Set<Notifier> notifiers,
                                      JmsTemplate jmsTemplate,
                                      Topic alertTopic) {
        this.alertDao = alertDao;
        this.notifierMapping = notifiers.stream().collect(Collectors.toMap(Notifier::getNotificationChannel, Function.identity()));
        this.jmsTemplate = jmsTemplate;
        this.alertTopic = alertTopic;
    }

    @Override
    public List<PrometheusAlertPO> saveAlerts(PrometheusWebHookDTO dto) {
        try {
            List<PrometheusAlertPO> alerts = convertToPrometheusAlertList(dto);
            for(PrometheusAlertPO alert : alerts) {
                alertDao.addAlert(alert);
            }
            return alerts;
        } catch (Exception e) {
            log.error("saveAlerts error! {}", dto, e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<PrometheusAlertInfoDTO> getAlerts(QueryAlertsRequestDTO dto) {
        String startTime = DateFormatUtils.format(dto.getStartTime(), DATETIME_PATTERN);
        String endTime = DateFormatUtils.format(dto.getEndTime(), DATETIME_PATTERN);
        List<PrometheusAlertPO> alertList = alertDao.getAlerts(dto.getServices(), startTime, endTime, 1);
        return alertList.stream()
                .map(alert -> PrometheusAlertInfoDTO.builder()
                    .alertname(alert.getAlertname())
                    .hostname(alert.getHostname())
                    .instance(alert.getInstance())
                    .startTime(DateFormatUtils.format(alert.getStartTime(), DATETIME_PATTERN))
                    .warningLevel(alert.getWarningLevel())
                    .warningService(alert.getWarningService())
                    .status(alert.getStatus())
                    .summary(alert.getSummary())
                    .build())
                .collect(Collectors.toList());
    }

    @Override
    public void sendAlerts(SendAlertsRequestDTO dto) {
        NotificationChannel notificationChannel = NotificationChannel.valueOf(dto.getNotificationChannel());
        if(ArrayUtils.isNotEmpty(dto.getAlertIds())) {
            List<PrometheusAlertPO> alerts = alertDao.getAlertsByIds(dto.getAlertIds());

            AlertExtraInfo extraInfo = new AlertExtraInfo();
            extraInfo.setReceiverIds(dto.getReceiverIds());
            extraInfo.setCcIds(dto.getCcIds());

            sendAlert(notificationChannel, alerts, extraInfo);
            updateAlerts(dto);
        }
    }

    private void updateAlerts(SendAlertsRequestDTO dto) {
        alertDao.updateAlerts(
                dto.getAlertIds(),
                StringUtils.join(dto.getReceiverIds(), ','),
                StringUtils.join(dto.getCcIds(), ','),
                NotificationChannel.valueOf(dto.getNotificationChannel()));
    }

    @Override
    public void pushAlerts(List<PrometheusAlertPO> alerts) {
        alerts.forEach(alert -> jmsTemplate.convertAndSend(alertTopic, alert));
    }

    private void sendAlert(NotificationChannel notificationChannel, List<PrometheusAlertPO> alerts, AlertExtraInfo extraInfo) {
        notifierMapping.get(notificationChannel).sendAlerts(alerts, extraInfo);
    }

    private List<PrometheusAlertPO> convertToPrometheusAlertList(PrometheusWebHookDTO dto) throws Exception {
        List<PrometheusAlertPO> alertList = null;
        Alert[] alerts = dto.getAlerts();
        if(ArrayUtils.isNotEmpty(alerts)) {
            alertList = new ArrayList<>(alerts.length);

            String startsAt;
            Date startTime;
            Map<String, String> labels;
            Map<String, String> annotations;
            PrometheusAlertPO prometheusAlertPO;

            for(Alert alert : alerts) {
                startsAt = alert.getStartsAt().substring(0, alert.getStartsAt().indexOf("."));
                startTime = DateFormatUtils.ISO_8601_EXTENDED_DATETIME_FORMAT.parse(startsAt);
                labels = alert.getLabels();
                annotations = alert.getAnnotations();
                prometheusAlertPO = new PrometheusAlertPO();

                // 存储label信息
                prometheusAlertPO.setAlertname(labels.remove(PrometheusLabel.alertname.name()));
                prometheusAlertPO.setHostname(labels.remove(PrometheusLabel.hostname.name()));
                prometheusAlertPO.setInstance(labels.remove(PrometheusLabel.instance.name()));
                prometheusAlertPO.setJob(labels.remove(PrometheusLabel.job.name()));
                prometheusAlertPO.setReceiverGroup(labels.remove(PrometheusLabel.receiver_group.name()));
                prometheusAlertPO.setWarningLevel(labels.remove(PrometheusLabel.warning_level.name()));
                prometheusAlertPO.setService(labels.remove(PrometheusLabel.service.name()));
                prometheusAlertPO.setWarningService(labels.remove(PrometheusLabel.warning_service.name()));
                prometheusAlertPO.setExtraLabels(labels.isEmpty() ? null : JSON.toJSONString(labels));

                // 存储annotation信息
                prometheusAlertPO.setSummary(annotations.remove(PrometheusAnnotation.summary.name()));
                prometheusAlertPO.setGrafanaLink(appendTimestampForUrl(PrometheusAnnotation.grafana_link, annotations, startTime));
                prometheusAlertPO.setSkyeLink(appendTimestampForUrl(PrometheusAnnotation.skye_link, annotations, startTime));
                prometheusAlertPO.setExtraAnnotations(annotations.isEmpty() ? null : JSON.toJSONString(annotations));

                prometheusAlertPO.setStatus(alert.getStatus());
                prometheusAlertPO.setStartTime(startTime);
                prometheusAlertPO.setVersion(Integer.parseInt(dto.getVersion()));
                prometheusAlertPO.setGroupLabels(MapUtils.isEmpty(dto.getGroupLabels())
                        ? null : JSON.toJSONString(dto.getGroupLabels()));

                alertList.add(prometheusAlertPO);
            }
        }

        return alertList == null ? Collections.emptyList() : alertList;
    }

    private String appendTimestampForUrl(PrometheusAnnotation annotation, Map<String, String> annotations, Date startTime) {
        String url = annotations.remove(annotation.name());
        if(StringUtils.isEmpty(url)) {
            return null;
        }

        switch (annotation) {
            case grafana_link: return appendTimestampForGrafana(url, startTime);
            case skye_link: return appendTimestampForSkye(url, startTime);
            default: return null;
        }
    }

    private String appendTimestampForGrafana(String url, Date startTime) {
        // 基准时间
        long baseTimestamp = startTime.getTime();

        // 基准时间的前10分钟作为起始时间
        long startTimestamp = baseTimestamp - 1000 * 60 * 10;

        // 基准时间的后10分钟作为结束时间
        long endTimestamp = baseTimestamp + 1000 * 60 * 10;

        return url + "&from=" + startTimestamp + "&to=" + endTimestamp;
    }

    private String appendTimestampForSkye(String url, Date startTime) {
        // 基准时间
        long baseTimestamp = startTime.getTime();

        // 基准时间的前1分钟作为起始时间
        long startTimestamp = baseTimestamp - 1000 * 60;

        // 基准时间的后5分钟作为结束时间
        long endTimestamp = baseTimestamp + 1000 * 60 * 5;

        return url + "&start=" + startTimestamp + "&end=" + endTimestamp;
    }
}
