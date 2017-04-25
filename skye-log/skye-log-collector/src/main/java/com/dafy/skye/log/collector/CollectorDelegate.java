package com.dafy.skye.log.collector;

import com.dafy.skye.log.collector.metrics.CollectorMetrics;
import com.dafy.skye.log.collector.storage.StorageComponent;
import com.dafy.skye.log.core.logback.SkyeLogEvent;

import java.util.List;

/**
 * Created by Caedmon on 2017/4/25.
 */
public class CollectorDelegate {
    private StorageComponent storage;
    private CollectorMetrics metrics;
    public CollectorDelegate(StorageComponent storage, CollectorMetrics metrics){
        this.storage=storage;
        this.metrics=metrics;
    }
    public void acceptEvent(SkyeLogEvent event){
        storage.save(event);
    }

    public void acceptEvents(List<SkyeLogEvent> events){
        storage.batchSave(events);
    }
    public static class Builder{
        private StorageComponent storage;
        private CollectorMetrics metrics;
        public Builder storage(StorageComponent storage){
            this.storage =storage;
            return this;
        }
        public Builder metrics(CollectorMetrics metrics){
            this.metrics=metrics;
            return this;
        }
        public CollectorDelegate build(){
            return new CollectorDelegate(this.storage,this.metrics);
        }
    }
}
