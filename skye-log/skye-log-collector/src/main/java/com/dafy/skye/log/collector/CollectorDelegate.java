package com.dafy.skye.log.collector;

import com.dafy.skye.log.collector.metrics.CollectorMetrics;
import com.dafy.skye.log.collector.storage.StorageComponent;
import com.dafy.skye.log.core.SkyeLogEventCodec;
import com.dafy.skye.log.core.SkyeLogEventDeserializer;
import com.dafy.skye.log.core.logback.SkyeLogEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Caedmon on 2017/4/25.
 * 收集器代理
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
        metrics.incrementMessages(1);
    }
    public void acceptEvents(List<SkyeLogEvent> events){
        storage.batchSave(events);
        metrics.incrementMessages(events.size());
    }

    public void accpetEvent(byte[] eventBytes,SkyeLogEventCodec codec){
        metrics.incrementBytes(eventBytes.length);
        SkyeLogEvent event=codec.decode(eventBytes);
        if(event==null){
            metrics.incrementMessageDropped(1);
        }else{
            acceptEvent(event);
        }
    }
    public void acceptEvents(List<byte[]> eventBytes, SkyeLogEventCodec codec){
        int droppedQuantity=0;
        int bytesQuantity=0;
        List<SkyeLogEvent> events=new ArrayList<>(eventBytes.size());
        for(byte[] bytes:eventBytes){
            bytesQuantity+=bytes.length;
            SkyeLogEvent event=codec.decode(bytes);
            if(event==null){
                droppedQuantity++;
            }else{
                events.add(event);
            }
        }
        metrics.incrementMessageDropped(droppedQuantity);
        metrics.incrementBytes(bytesQuantity);
        acceptEvents(events);
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
