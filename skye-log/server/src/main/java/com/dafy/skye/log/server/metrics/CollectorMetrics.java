package com.dafy.skye.log.server.metrics;

/**
 * Created by Caedmon on 2017/4/25.
 * 收集统计接口
 */
public interface CollectorMetrics {


    /**
     * 统计数据流
     * */
    void incrementBytes(int quantity);
    /**
     * 统计消息处理数
     * */
    void incrementMessages(int quantity);
    /**
     * 统计消息丢失数
     * */
    void incrementMessageDropped(int quantity);

    void incrementMessageError(int quantity);
}
