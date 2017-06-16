package com.dafy.skye.log.server.metrics;

/**
 * Created by Caedmon on 2017/5/8.
 * 统计数据存入内存,重启后丢失
 */
public class MemoryCollectorMetrics implements CollectorMetrics{
    @Override
    public void incrementBytes(int quantity) {

    }

    @Override
    public void incrementMessages(int quantity) {

    }

    @Override
    public void incrementMessageDropped(int quantity) {

    }
}
