package com.dafy.skye.prometheus;

import io.prometheus.client.hotspot.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SkyeExports {

    private static final Logger logger = LoggerFactory.getLogger(SkyeExports.class);

    private static boolean initialized = false;

    private static void initializeDefaultExports() {
        new StandardExports().register();
        new MemoryPoolsExports().register();
        new BufferPoolsExports().register();
        new GarbageCollectorExports().register();
        new ThreadExports().register();
        new ClassLoadingExports().register();
        new VersionInfoExports().register();
    }

    // 因为这里列出的组件可能在接入的项目中没有用到，所以这里列出的export都不是必需的，这里约定当没有对应的组件时，对应的exporter应
    // 抛出UnsupportedOperationException异常，当前类应捕获该异常并输出相关的提示信息。
    // exporter应当尽量全部采用JMX的方式来获取监控数据，避免耦合而引入这些组件相关的依赖。
    private static void initializeSkyeExports() {
        initializeDruidExports();
    }

    public static synchronized void initialize() {
        if (!initialized) {
            initializeDefaultExports();
            initializeSkyeExports();
            initialized = true;
        }
    }

    private static void initializeDruidExports() {
        try {
            new DruidExports().register();
        } catch (UnsupportedOperationException e) {
            logger.warn("skip druid exports!");
        }
    }
}
