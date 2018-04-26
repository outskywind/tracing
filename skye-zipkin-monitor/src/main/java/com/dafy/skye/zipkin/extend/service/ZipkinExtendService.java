package com.dafy.skye.zipkin.extend.service;

import com.dafy.skye.zipkin.extend.dto.*;

import java.util.List;
import java.util.Set;

/**
 * Created by Caedmon on 2017/6/20.
 */
public interface ZipkinExtendService {

    Set<String> getServices(BasicQueryRequest request);

    TraceQueryResult getTraces(BasicQueryRequest request);

    ServiceTimeSeriesResult getServiceTimeSeries(BasicQueryRequest request);

    List<ServiceMonitorMetric> getServiceMonitorMetrics(BasicQueryRequest request);

    List<String> getServiceInterfaces(String serviceName);
}
