package com.dafy.skye.klog.collector.storage;

import com.dafy.skye.klog.core.logback.KLogEvent;

import java.util.Collection;

/**
 * Created by Caedmon on 2017/4/14.
 */
public interface StorageComponent {
    /**
     * 开始
     * */
    void start();
    /**
     * 保存
     * */
    void save(KLogEvent event);
    /**
     * 批量保存
     * */
    void batchSave(Collection<KLogEvent> events);
    /**
     * 停止
     * */
    void shutdown();
}
