package com.dafy.skye.log.server.collector;

import com.dafy.skye.log.server.collector.filter.CollectFilter;
import com.dafy.skye.log.server.collector.filter.FilterChain;
import com.dafy.skye.log.server.metrics.CollectorMetrics;
import com.dafy.skye.log.server.storage.StorageComponent;
import com.dafy.skye.log.core.SkyeLogEventCodec;
import com.dafy.skye.log.core.logback.SkyeLogEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Caedmon on 2017/4/25.
 * 收集器代理
 */
public class CollectorDelegate {
    private StorageComponent storage;
    private CollectorMetrics metrics;

    private FilterChain chain = new FilterChain();

    private static final Logger log= LoggerFactory.getLogger(CollectorDelegate.class);
    public CollectorDelegate(StorageComponent storage, CollectorMetrics metrics){
        this.storage=storage;
        this.metrics=metrics;
        this.chain.addFilter(new InvokeStorage());
    }


    class InvokeStorage implements CollectFilter{

        @Override
        public Object filter(List<SkyeLogEvent> event, Iterator<CollectFilter> filters) {
            return doAcceptEvents(event);
        }

        @Override
        public int getOrder() {
            return Integer.MAX_VALUE;
        }

        @Override
        public void setOrder(int order) {
        }
    }

    public void addFilters(List<CollectFilter> filters){
        for(CollectFilter filter:filters){
            this.chain.addFilter(filter);
        }
    }

    public void addFilter(CollectFilter filter){
        this.chain.addFilter(filter);
    }



    public FilterChain getChain() {
        return chain;
    }

    public void setChain(FilterChain chain) {
        this.chain = chain;
    }

    public void invokeFilters(List<SkyeLogEvent> events){
        this.chain.doNextFilter(events);
    }

    /*public void acceptEvent(SkyeLogEvent event){


        try{
            storage.save(event);
        }catch (Exception e){
            log.error("Accept event error: ",e);
            metrics.incrementMessageError(1);
            return;
        }
        metrics.incrementMessages(1);
    }*/

    public boolean acceptEvents(List<SkyeLogEvent> events) {
            return (boolean)this.chain.doNextFilter(events);
    }

    public boolean doAcceptEvents(List<SkyeLogEvent> events){
        try{
            storage.batchSave(events);
        }catch (Exception e){
            log.error("Accept events error：",e);
            metrics.incrementMessageError(1);
            return false;
        }
        metrics.incrementMessages(events.size());
        return true;
    }

    /*public void accpetEvent(byte[] eventBytes,SkyeLogEventCodec codec){
        metrics.incrementBytes(eventBytes.length);
        try{
            SkyeLogEvent event=codec.decode(eventBytes);
            if(event==null){
                metrics.incrementMessageDropped(1);
            }else{
                acceptEvent(event);
            }
        }catch (Exception e){
            log.error("decode error:",e);
        }

    }*/
    public boolean acceptEvents(List<byte[]> eventBytes, SkyeLogEventCodec codec){
        if(eventBytes==null||eventBytes.isEmpty()){
            log.warn("Event bytes is empty");
            return false;
        }
        int droppedQuantity=0;
        int bytesQuantity=0;
        List<SkyeLogEvent> events=new ArrayList<>(eventBytes.size());
        for(byte[] bytes:eventBytes){
            bytesQuantity+=bytes.length;
            try{
                SkyeLogEvent event=codec.decode(bytes);
                if(event==null){
                    droppedQuantity++;
                }else{
                    events.add(event);
                }
            }catch(Exception e){
                log.error("decode exception:",e);
            }
        }
        metrics.incrementMessageDropped(droppedQuantity);
        metrics.incrementBytes(bytesQuantity);
        if(eventBytes==null||eventBytes.isEmpty()){
            log.warn("Event bytes is empty");
            return false;
        }
        return acceptEvents(events);
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
