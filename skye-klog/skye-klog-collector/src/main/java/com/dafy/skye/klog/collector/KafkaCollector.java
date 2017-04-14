package com.dafy.skye.klog.collector;

import com.dafy.skye.klog.collector.consumer.ConsumerConfig;
import com.dafy.skye.klog.collector.storage.StorageComponent;
import com.dafy.skye.klog.core.logback.KLogEvent;
import offset.OffsetComponent;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Caedmon on 2017/4/14.
 */
public class KafkaCollector implements Runnable{
    private ConsumerConfig kafkaTopicConfig;
    private KafkaConsumer<String,Object> kafkaConsumer;
    private OffsetComponent offsetComponent;
    private StorageComponent storageComponent;
    private AtomicBoolean closed=new AtomicBoolean(false);
    private static final Logger log= LoggerFactory.getLogger(KafkaCollector.class);
    public KafkaCollector(ConsumerConfig kafkaTopicConfig,
                          KafkaConsumer<String,Object> kafkaConsumer,
                          StorageComponent storageComponent,
                          OffsetComponent offsetComponent){
        this.kafkaTopicConfig=kafkaTopicConfig;
        this.kafkaConsumer=kafkaConsumer;
        this.storageComponent=storageComponent;
        this.offsetComponent =offsetComponent;
    }

    @Override
    public void run() {
        this.storageComponent.start();
        final String topicName=kafkaTopicConfig.getTopic();
        final int partition=kafkaTopicConfig.getPartition();
        long pullInterval=kafkaTopicConfig.getPullInterval();
        TopicPartition topicPartition=new TopicPartition(topicName,partition);
        kafkaConsumer.assign(Arrays.asList(topicPartition));

        //读取offset
        Long offset= offsetComponent.getOffset();
        log.info("First get offset:partition={},offset={}",partition,offset);
        try{
            kafkaConsumer.seek(topicPartition,offset);
            while (!closed.get()) {
                //从kafka pull 消息
                ConsumerRecords<String, Object> records = kafkaConsumer.poll(pullInterval);
                long lastRecordOffset=offset;
                for (ConsumerRecord<String, Object> record : records) {
                    if(record.offset()<=offset){
                        continue;
                    }
                    KLogEvent event=(KLogEvent) record.value();
                    storageComponent.save(event);
                    lastRecordOffset=record.offset();
                }
                if(!records.isEmpty()){
                    log.debug("pull log success:partition={},offset={},size={}",
                            partition,lastRecordOffset,records.count());
                    kafkaConsumer.commitAsync(new OffsetCommitCallback() {
                        @Override
                        public void onComplete(Map<TopicPartition, OffsetAndMetadata> offsets, Exception exception) {
                            if(exception!=null){
                                exception.printStackTrace();
                            }
                        }
                    });
                }
                if(lastRecordOffset>offset){
                    offsetComponent.setOffset(lastRecordOffset);
                }
            }
        }catch (Throwable e){
            if(!closed.get()){
                log.error("pull log error:partition={},offset={}",partition,offset,e);
            }
        }finally {
            kafkaConsumer.close();
            this.storageComponent.shutdown();
        }
    }
    public static class Builder{
        private ConsumerConfig kafkaTopicConfig;
        private KafkaConsumer<String,Object> kafkaConsumer;
        private StorageComponent storageComponent;
        private OffsetComponent offsetComponent;
        public static Builder create(){
            return new Builder();
        }
        public Builder kafkaConsumer(KafkaConsumer<String,Object> kafkaConsumer){
            this.kafkaConsumer=kafkaConsumer;
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
        public Builder kafkaTopicConfig(ConsumerConfig kafkaTopicConfig){
            this.kafkaTopicConfig=kafkaTopicConfig;
            return this;
        }
        public KafkaCollector build(){
            KafkaCollector collector=new KafkaCollector(
                    this.kafkaTopicConfig,
                    this.kafkaConsumer,
                    this.storageComponent,
                    this.offsetComponent
            );
            return collector;
        }
    }
}
