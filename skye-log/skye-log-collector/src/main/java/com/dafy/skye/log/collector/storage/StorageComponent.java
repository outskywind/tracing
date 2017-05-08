package com.dafy.skye.log.collector.storage;

import com.dafy.skye.log.collector.CollectorComponent;
import com.dafy.skye.log.collector.storage.domain.SkyeLogEntity;
import com.dafy.skye.log.collector.storage.query.LogQueryRequest;
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

    List<SkyeLogEntity> query(LogQueryRequest request);
}
