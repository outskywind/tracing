package com.dafy.skye.alertmanager.controller;

import com.dafy.base.nodepencies.model.Response;
import com.dafy.skye.alertmanager.dto.prometheus.PrometheusWebHookDTO;
import com.dafy.skye.alertmanager.po.PrometheusAlertPO;
import com.dafy.skye.alertmanager.service.PrometheusAlertService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/webhook")
public class WebHookController {

    private final PrometheusAlertService prometheusAlertService;

    @Autowired
    public WebHookController(PrometheusAlertService prometheusAlertService) {
        this.prometheusAlertService = prometheusAlertService;
    }

    @ResponseBody
    @PostMapping("prometheus")
    public Response<?> prometheus(@RequestBody PrometheusWebHookDTO request){
        log.info("webhook prometheus request={}", request);
        List<PrometheusAlertPO> alerts = prometheusAlertService.saveAlerts(request);
        prometheusAlertService.pushAlerts(alerts);
        return Response.EMPTY_SUCCESS;
    }

}
