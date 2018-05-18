package com.dafy.skye.log.server.storage;

import com.dafy.skye.log.core.logback.SkyeLogEvent;
import com.dafy.skye.log.server.collector.CollectorComponent;
import com.dafy.skye.log.server.storage.query.CountMetric;
import com.dafy.skye.log.server.storage.query.LogQueryResult;
import com.dafy.skye.log.server.storage.query.LogSearchRequest;

import java.util.Collection;
import java.util.List;

/**
 * Created by Caedmon on 2017/4/14.
 * 存储组件接口,存储上报日志
 */
public interface StorageComponent extends CollectorComponent {

    void save(SkyeLogEvent event) throws Exception;

    void batchSave(Collection<SkyeLogEvent> events) throws Exception;

    LogQueryResult query(LogSearchRequest request);

    List<CountMetric> countSeries(LogSearchRequest request);
}
