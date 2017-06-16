package com.dafy.skye.log.server.collector;

/**
 * Created by Caedmon on 2017/4/16.
 * 收集器子组件,通过一系列的收集器子组件来完成日志收集流程
 */
public interface CollectorComponent {
    void start();

    void stop();

}
