package com.dafy.skye.klog.collector.consumer;

import com.dafy.skye.klog.collector.CollectorComponent;
import com.dafy.skye.klog.core.logback.KLogEvent;

import java.util.List;

/**
 * Created by Caedmon on 2017/4/14.
 * 消费组件接口,消费日志事件消息
 */
public interface ConsumerComponent  extends CollectorComponent{

    void seek(long offset);

    PollResult poll();

    void commit(long offset);
    /**
     * @return 当前消费指针,如果返回0 要么没有调用seek() 要么出现异常情况
     * */
    long currentOffset();
}
