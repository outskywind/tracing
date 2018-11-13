package com.dafy.skye.alertmanager.controller;

import com.dafy.skye.alertmanager.constant.NotificationChannel;
import com.dafy.skye.alertmanager.dto.QueryAlertsRequestDTO;
import com.dafy.skye.alertmanager.dto.SendAlertsRequestDTO;
import com.dafy.skye.alertmanager.dto.prometheus.PrometheusAlertInfoDTO;
import com.dafy.skye.alertmanager.service.PrometheusAlertService;
import com.dafy.base.nodepencies.model.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/prometheus")
public class PrometheusController extends AbstractController {

    private final PrometheusAlertService prometheusAlertService;

    @Autowired
    public PrometheusController(PrometheusAlertService prometheusAlertService) {
        this.prometheusAlertService = prometheusAlertService;
    }

    @ResponseBody
    @PostMapping("queryAlerts")
    public Response<List<PrometheusAlertInfoDTO>> queryAlerts(@RequestBody QueryAlertsRequestDTO request){
        log.info("prometheus queryAlerts request={}", request);
        Response response = checkQueryAlertsRequestDTO(request);
        if(response != null) {
            return response;
        }
        return new Response<>(prometheusAlertService.getAlerts(request));
    }

    @ResponseBody
    @PostMapping("sendAlerts")
    public Response<?> sendAlerts(@RequestBody SendAlertsRequestDTO request){
        Response<?> response = checkSendAlertsRequestDTO(request);
        if(response != null) {
            return response;
        }
        prometheusAlertService.sendAlerts(request);
        return Response.EMPTY_SUCCESS;
    }

    private Response<?> checkSendAlertsRequestDTO(SendAlertsRequestDTO dto) {
        if(StringUtils.isEmpty(dto.getNotificationChannel()) || ArrayUtils.isEmpty(dto.getAlertIds())) {
            return getResponseForParamError();
        }

        try {
            NotificationChannel.valueOf(dto.getNotificationChannel());
        } catch (IllegalArgumentException e) {
            return getResponseForParamError();
        }

        return null;
    }

    private Response<?> checkQueryAlertsRequestDTO(QueryAlertsRequestDTO dto) {
        if(dto.getStartTime() <= 0) {
            return getResponseForParamError();
        }
        return null;
    }
}
