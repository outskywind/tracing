package com.dafy.skye.log.server.storage;

import com.dafy.skye.log.server.collector.CollectorComponent;
import com.dafy.skye.log.server.storage.query.LogQueryRequest;
import com.dafy.skye.log.server.storage.query.LogQueryResult;
import com.dafy.skye.log.core.logback.SkyeLogEvent;

import java.util.Collection;
import java.util.List;

/**
 * Created by Caedmon on 2017/4/14.
 * 存储组件接口,存储上报日志
 */
public interface StorageComponent extends CollectorComponent {

    void save(SkyeLogEvent event);

    void batchSave(Collection<SkyeLogEvent> events);

    LogQueryResult query(LogQueryRequest request);

    List<String> getServiceNames();



}
