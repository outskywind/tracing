package com.dafy.skye.zipkin.extend.dto;

import java.util.List;

/**
 * Created by quanchengyun on 2018/4/23.
 */
public class ServiceMonitorMetric extends MonitorMetric {

    public List<MonitorMetric> servers;

    public List<MonitorMetric> getServers() {
        return servers;
    }

}
