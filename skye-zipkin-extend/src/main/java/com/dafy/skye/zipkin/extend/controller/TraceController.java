package com.dafy.skye.zipkin.extend.controller;

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
    @RequestMapping("/metrics")
    TraceMetricsResult getTraceMetrics(@RequestBody  TraceQueryRequest request){
        return zipkinExtendService.getTracesMetrics(request);
    }
    @RequestMapping("/span/metrics")
    SpanMetricsResult getSpanMetrics(@RequestBody  TraceQueryRequest request){
        return zipkinExtendService.getSpansMetrics(request);
    }
    @RequestMapping("/list")
    TraceQueryResult getTraces(@RequestBody  TraceQueryRequest request){
        return zipkinExtendService.getTraces(request);
    }
    @RequestMapping("/services")
    Set<String> getServiceNames(@RequestBody ServiceNameQueryRequest request){
        return zipkinExtendService.getServices(request);
    }
    @RequestMapping("/spans")
    Set<String> getSpanNames(@RequestBody SpanNameQueryRequest request){
        return zipkinExtendService.getSpans(request);
    }
    /**
     * span调用时间序列图
     * @param request
     * @return
     */
    @RequestMapping("/span/series")
    SpanTimeSeriesResult getSpanTimeSeries(@RequestBody TraceQueryRequest request){
        return zipkinExtendService.getMultiSpansTimeSeries(request);
    }

    public ZipkinExtendService getZipkinExtendService() {
        return zipkinExtendService;
    }

    @Autowired
    public void setZipkinExtendService(ZipkinExtendService zipkinExtendService) {
        this.zipkinExtendService = zipkinExtendService;
    }
}
