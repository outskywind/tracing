package com.dafy.skye.klog.collector.storage;

import com.dafy.skye.klog.collector.CollectorComponent;
import com.dafy.skye.klog.core.logback.KLogEvent;

import java.util.Collection;

/**
 * Created by Caedmon on 2017/4/14.
 * 存储组件接口,存储上报日志
 */
public interface StorageComponent extends CollectorComponent{
    /**
     * 保存
     * */
    void save(KLogEvent event);
    /**
     * 批量保存
     * */
    void batchSave(Collection<KLogEvent> events);
}
