package com.dafy.skye.alertmanager.notifier;

import com.dafy.skye.alertmanager.constant.NotificationChannel;
import com.dafy.skye.alertmanager.dto.AlertExtraInfo;
import com.dafy.skye.alertmanager.po.PrometheusAlertPO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public abstract class AbstractNotifier implements Notifier {

    protected final NotificationChannel notificationChannel;

    public AbstractNotifier(NotificationChannel notificationChannel) {
        this.notificationChannel = notificationChannel;
    }

    @Override
    public final NotificationChannel getNotificationChannel() {
        return this.notificationChannel;
    }

    protected abstract String[] getUserIdsForReceivers(String service);

    @Override
    public void sendAlerts(List<PrometheusAlertPO> alerts, AlertExtraInfo extraInfo) {
        try {
            Map<String, List<PrometheusAlertPO>> map = alerts.stream().collect(Collectors.groupingBy(PrometheusAlertPO::getWarningService));
            for(Map.Entry<String, List<PrometheusAlertPO>> entry : map.entrySet()) {
                String warningService = entry.getKey();
                List<PrometheusAlertPO> alertList = entry.getValue();
                if(ArrayUtils.isEmpty(extraInfo.getReceiverIds())) {
                    extraInfo.setReceiverIds(getUserIdsForReceivers(warningService));
                }

                log.info("sendAlerts notificationChannel={}, warningService={}, extraInfo={}",
                        this.notificationChannel, warningService, extraInfo);

                if(ArrayUtils.isNotEmpty(extraInfo.getReceiverIds())) {
                    // 二次分组
                    Map<String, List<PrometheusAlertPO>> secondaryMap = groupAlertsInSameService(alertList);
                    if(MapUtils.isNotEmpty(secondaryMap)) {
                        for(Map.Entry<String, List<PrometheusAlertPO>> e : map.entrySet()) {
                            extraInfo.setGroupName(e.getKey());
                            convertAndSendAlert(e.getValue(), extraInfo);
                        }
                    } else {
                        convertAndSendAlert(alertList, extraInfo);
                    }
                }
            }
        } catch (Exception e) {
            log.error("sendAlerts error! notificationChannel={}, alerts={}", this.notificationChannel, alerts, e);
        }
    }

    private void convertAndSendAlert(List<PrometheusAlertPO> alerts, AlertExtraInfo extraInfo) {
        String alertContent = convertToText(alerts);
        if(StringUtils.isNoneBlank(alertContent)) {
            sendAlert(alertContent, extraInfo);
            log.info("sendAlerts succ! alertContent={}", alertContent);
        }
    }

    protected abstract Map<String, List<PrometheusAlertPO>> groupAlertsInSameService(List<PrometheusAlertPO> alerts);

    protected abstract String convertToText(List<PrometheusAlertPO> alerts);

    protected abstract void sendAlert(String alertContent, AlertExtraInfo extraInfo);
}
