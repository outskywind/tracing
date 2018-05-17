package com.dafy.skye.zipkin.extend.web.controller;

import com.dafy.skye.zipkin.extend.dto.*;
import com.dafy.skye.zipkin.extend.service.ZipkinExtendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

/**
 * Created by Caedmon on 2017/6/21.
 */
@RestController
@RequestMapping(value="/api/v1/trace", method = RequestMethod.POST)
public class TraceController {

    ZipkinExtendService zipkinExtendService;

    /*@RequestMapping("/span/metrics")
    SpanMetricsResult getSpanMetrics(@RequestBody  TraceQueryRequest request){
        return zipkinExtendService.getSpansMetrics(request);
    }*/
    @RequestMapping("/list")
    TraceQueryResult getTraces(@RequestBody  BasicQueryRequest request){
        return zipkinExtendService.getTraces(request);
    }
    @RequestMapping("/services")
    Set<String> getServiceNames(@RequestBody BasicQueryRequest request){
        return zipkinExtendService.getServices(request);
    }

    /**
     * span调用时间序列图
     * @param request
     * @return
     */
    @RequestMapping("/span/series")
    TimeSeriesResult getServiceTimeSeries(@RequestBody BasicQueryRequest request){
        return zipkinExtendService.getServiceTimeSeries(request);
    }

    public ZipkinExtendService getZipkinExtendService() {
        return zipkinExtendService;
    }

    @Autowired
    public void setZipkinExtendService(ZipkinExtendService zipkinExtendService) {
        this.zipkinExtendService = zipkinExtendService;
    }
}
