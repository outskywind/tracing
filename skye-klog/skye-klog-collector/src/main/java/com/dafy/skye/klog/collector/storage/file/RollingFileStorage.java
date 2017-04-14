package com.dafy.skye.klog.collector.storage.file;

import com.dafy.skye.klog.collector.storage.StorageComponent;
import com.dafy.skye.klog.core.logback.KLogEvent;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Caedmon on 2017/4/14.
 */
public class RollingFileStorage implements StorageComponent {
    /**
     * 不同appender的缓存
     * */
    private static final Map<String,RollingFileAppender> appenderCache=new ConcurrentHashMap<>();
    private RollingFileStorageConfig config;
    public RollingFileStorage(RollingFileStorageConfig config){
        this.config=config;
    }
    @Override
    public void start() {
        if(!config.loggerContext.isStarted()){
            config.loggerContext.start();
        }
    }

    @Override
    public void save(KLogEvent event) {
        RollingFileAppender appender=getAppender(event);
        appender.doAppend(event);
    }

    @Override
    public void batchSave(Collection<KLogEvent> events) {
        for(KLogEvent event:events){
            save(event);
        }
    }

    @Override
    public void shutdown() {
    }
    /**
     * 获取Appender
     * */
    private RollingFileAppender getAppender(KLogEvent event){
        String appenderName=event.getServiceName()+"-"+event.getAddress();
        RollingFileAppender appender=appenderCache.get(appenderName);
        if(appender==null) {
            appender=new RollingFileAppender(appenderName,event.getServiceName(),
                    event.getAddress(),config);
            appenderCache.put(appenderName, appender);
            appender.start();
        }
        return appender;
    }
}
