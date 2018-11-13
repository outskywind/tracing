package com.dafy.skye.alertmanager.notifier;

import com.dafy.skye.alertmanager.config.TapdConfigProperties;
import com.dafy.skye.alertmanager.constant.NotificationChannel;
import com.dafy.skye.alertmanager.dto.AlertExtraInfo;
import com.dafy.skye.alertmanager.po.PrometheusAlertPO;
import com.dafy.base.channel.tapd.api.TapdApiClient;
import com.dafy.base.channel.tapd.api.TapdApiClientFactory;
import com.dafy.base.channel.tapd.api.dto.AddBugRequest;
import com.dafy.base.channel.tapd.api.dto.AddBugResponse;
import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class TapdNotifier extends DefaultNotifier {

    private final TapdApiClient CLIENT;

    private final TapdConfigProperties configProperties;

    @Autowired
    public TapdNotifier(TapdConfigProperties configProperties) {
        super(NotificationChannel.TAPD);
        this.configProperties = configProperties;
        CLIENT = TapdApiClientFactory.getInstance().getApiClient();
    }

    @Override
    protected String[] getUserIdsForReceivers(String service) {
        return null;
    }

    @Override
    protected Map<String, List<PrometheusAlertPO>> groupAlertsInSameService(List<PrometheusAlertPO> alerts) {
        return alerts.stream().collect(Collectors.groupingBy(PrometheusAlertPO::getAlertname));
    }

    @Override
    protected void sendAlert(String alertContent, AlertExtraInfo extraInfo) {
        AddBugRequest request = new AddBugRequest();
        request.setWorkspaceId(configProperties.getWorkspaceId());
        request.setCurrentOwner(joinIds(extraInfo.getReceiverIds()));
        request.setCc(joinIds(extraInfo.getCcIds()));
        request.setDescription(alertContent);
        request.setTitle(extraInfo.getGroupName());

        AddBugResponse response = CLIENT.addBug(request);
        if(!response.isSucc()) {
            log.error("sendAlert error! status={}, info={}", response.getStatus(), response.getInfo());
        }
    }

    private String joinIds(String[] ids) {
        return ArrayUtils.isEmpty(ids) ? null : Joiner.on(',').join(ids);
    }
}
