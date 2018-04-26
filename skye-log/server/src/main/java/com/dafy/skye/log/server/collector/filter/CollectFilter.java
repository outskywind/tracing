package com.dafy.skye.log.server.collector.filter;

import com.dafy.skye.log.core.logback.SkyeLogEvent;

import java.util.Iterator;
import java.util.List;

/**
 * Created by quanchengyun on 2018/3/30.
 */
public interface CollectFilter {

    Object filter(List<SkyeLogEvent> event, Iterator<CollectFilter> filters);

    int getOrder();

    void setOrder(int order);

}
