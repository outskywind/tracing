package com.dafy.skye.log.collector.storage.rolling;

import com.dafy.skye.log.collector.storage.StorageComponent;
import com.dafy.skye.log.collector.storage.domain.SkyeLogEntity;
import com.dafy.skye.log.collector.storage.query.LogQueryRequest;
import com.dafy.skye.log.core.logback.SkyeLogEvent;

import java.util.Collection;
import java.util.List;
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
    public void stop() {
//        config.loggerContext.stop();
    }

    @Override
    public void save(SkyeLogEvent event) {
        RollingFileAppender appender=getAppender(event);
        appender.doAppend(event);
    }

    @Override
    public void batchSave(Collection<SkyeLogEvent> events) {
        for(SkyeLogEvent event:events){
            save(event);
        }
    }

    /**
     * 获取Appender
     * */
    private RollingFileAppender getAppender(SkyeLogEvent event){
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

    @Override
    public List<SkyeLogEntity> query(LogQueryRequest request) {
        throw new UnsupportedOperationException("Temporary support");
    }
}
