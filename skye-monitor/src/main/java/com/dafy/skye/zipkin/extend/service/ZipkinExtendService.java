package com.dafy.skye.zipkin.extend.service;

import com.dafy.skye.zipkin.extend.dto.*;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Created by Caedmon on 2017/6/20.
 */
public interface ZipkinExtendService {

    Set<String> getServices(BasicQueryRequest request);

    List<ServiceMonitorMetric> getServiceMonitorMetrics(BasicQueryRequest request);

    ServiceInfo getserviceinfo(String serviceName);

    Collection<TimeSeriesResult> getServiceSeries(ServiceSeriesRequest request);

    Collection<MonitorMetric> getInterfacesMonitorMetric(ServiceSeriesRequest request);

    Collection<SeriesMetric> getInterfaceSeries(InterfaceSeriesRequest request);

    List<Trace> getInterfaceTraces(BasicQueryRequest request);
}
