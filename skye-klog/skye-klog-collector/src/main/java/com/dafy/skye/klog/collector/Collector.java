package com.dafy.skye.klog.collector;

import com.dafy.skye.klog.collector.storage.StorageComponent;
import com.dafy.skye.klog.core.logback.KLogEvent;

import java.util.List;

/**
 * Created by Caedmon on 2017/4/25.
 */
public class Collector {
    private StorageComponent storage;
    public Collector(StorageComponent storage){
        this.storage=storage;
    }
    public void acceptEvent(KLogEvent event){
        storage.save(event);
    }

    public void acceptEvents(List<KLogEvent> events){
        storage.batchSave(events);
    }
    public static class Builder{
        private StorageComponent storageComponent;
        public Builder storage(StorageComponent storageComponent){
            this.storageComponent=storageComponent;
            return this;
        }
        public Collector build(){
            return new Collector(this.storageComponent);
        }
    }
}
