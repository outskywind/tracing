package com.dafy.skye.log.collector.storage;

import com.dafy.skye.log.collector.CollectorComponent;
import com.dafy.skye.log.core.logback.SkyeLogEvent;

import java.util.Collection;

/**
 * Created by Caedmon on 2017/4/14.
 * 存储组件接口,存储上报日志
 */
public interface StorageComponent extends CollectorComponent {
    /**
     * 保存
     * */
    void save(SkyeLogEvent event);
    /**
     * 批量保存
     * */
    void batchSave(Collection<SkyeLogEvent> events);
}
