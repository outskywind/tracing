package com.dafy.skye.klog.collector;

import com.dafy.skye.klog.collector.consumer.ConsumerComponent;
import com.dafy.skye.klog.collector.consumer.PollResult;
import com.dafy.skye.klog.collector.offset.OffsetComponent;
import com.dafy.skye.klog.collector.storage.StorageComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Caedmon on 2017/4/14.
 */
public class DefaultPartitionCollector extends AbstractCollectorComponent implements Runnable{
    private ConsumerComponent consumerComponent;
    private OffsetComponent offsetComponent;
    private StorageComponent storageComponent;
    private AtomicBoolean closed=new AtomicBoolean(false);
    private static final Logger log= LoggerFactory.getLogger(DefaultPartitionCollector.class);

    public DefaultPartitionCollector(CollectorPartitionConfig collectorPartitionConfig, ConsumerComponent consumerComponent,
                                     StorageComponent storageComponent,
                                     OffsetComponent offsetComponent) {
        this.consumerComponent=consumerComponent;
        this.storageComponent=storageComponent;
        this.offsetComponent =offsetComponent;
        setCollectorPartitionConfig(collectorPartitionConfig);
        this.consumerComponent.setCollectorPartitionConfig(getCollectorPartitionConfig());
        this.storageComponent.setCollectorPartitionConfig(getCollectorPartitionConfig());
        this.offsetComponent.setCollectorPartitionConfig(getCollectorPartitionConfig());
    }
    @Override
    public void start() {
        this.consumerComponent.start();
        this.storageComponent.start();
        this.offsetComponent.start();
    }

    @Override
    public void stop() {
        this.consumerComponent.stop();
        this.storageComponent.stop();
        this.offsetComponent.stop();
    }

    @Override
    public void run() {
        start();
        //读取offset
        Long offset= offsetComponent.getOffset();
        try{
            this.consumerComponent.seek(offset);
            while (!closed.get()) {
                //从kafka pull 消息
                PollResult poll=consumerComponent.poll();
                if(!poll.isEmpty()){
                    log.debug("pull log success:partition={},offset={},size={}",
                            this.collectorPartitionConfig.getPartition(),poll.getEndOffset(),poll.size());
                    this.storageComponent.batchSave(poll.getEvents());
                    this.consumerComponent.commit(poll.getEndOffset());
                }
                if(poll.getEndOffset()>offset){
                    offsetComponent.setOffset(poll.getEndOffset());
                }
            }
        }catch (Throwable e){
            if(!closed.get()){
                log.error("pull log error:partition={},offset={}",this.collectorPartitionConfig.getPartition(),offset,e);
            }
        }finally {
            stop();
        }
    }

    @Override
    public String toString() {
        return "DefaultPartitionCollector{" +
                "collectorPartitionConfig="+ collectorPartitionConfig +
                ", consumerComponent=" + consumerComponent +
                ", offsetComponent=" + offsetComponent +
                ", storageComponent=" + storageComponent +
                ", closed=" + closed +
                '}';
    }

    public static class Builder{
        private CollectorPartitionConfig collectorPartitionConfig;
        private ConsumerComponent consumerComponent;
        private StorageComponent storageComponent;
        private OffsetComponent offsetComponent;
        public static Builder create(){
            return new Builder();
        }
        public CollectorPartitionConfig collectorConfig(CollectorPartitionConfig collectorPartitionConfig){
            this.collectorPartitionConfig = collectorPartitionConfig;
            return collectorPartitionConfig;
        }
        public Builder consumerComponent(ConsumerComponent consumerComponent){
            this.consumerComponent=consumerComponent;
            return this;
        }
        public Builder storageComponent(StorageComponent storageComponent){
            this.storageComponent=storageComponent;
            return this;
        }
        public Builder offsetComponent(OffsetComponent offsetComponent){
            this.offsetComponent =offsetComponent;
            return this;
        }

        public DefaultPartitionCollector build(){
            DefaultPartitionCollector collector=new DefaultPartitionCollector(this.collectorPartitionConfig,this.consumerComponent,
                    this.storageComponent,
                    this.offsetComponent
            );
            return collector;
        }
    }
}
