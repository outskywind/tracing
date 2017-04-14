package com.dafy.skye.klog.collector.consumer;

import com.dafy.skye.klog.core.logback.KLogEvent;

import java.util.List;

/**
 * Created by Caedmon on 2017/4/14.
 */
public interface ConsumerComponent {

    boolean start();

    void setConsumerConfig(ConsumerConfig consumerConfig);

    void seek(long offset);

    List<KLogEvent> poll();

    void commit(long offset);
}
