package com.dafy.skye.alertmanager.notifier;

import com.dafy.skye.alertmanager.constant.NotificationChannel;
import com.dafy.skye.alertmanager.po.PrometheusAlertPO;
import com.dafy.skye.alertmanager.util.TemplateUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public abstract class DefaultNotifier extends AbstractNotifier {

    private static final String FTL_ALERT = "alert.ftl";

    public DefaultNotifier(NotificationChannel notificationChannel) {
        super(notificationChannel);
    }

    @Override
    protected Map<String, List<PrometheusAlertPO>> groupAlertsInSameService(List<PrometheusAlertPO> alerts) {
        return null;
    }

    // 根据模板文件将dto对象转换成文本内容
    @Override
    protected String convertToText(List<PrometheusAlertPO> alerts) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("alerts", alerts);
            return TemplateUtil.renderTemplate(FTL_ALERT, data);
        } catch (Exception e) {
            log.error("convertToText error! alerts={}", alerts, e);
            return null;
        }
    }
}
