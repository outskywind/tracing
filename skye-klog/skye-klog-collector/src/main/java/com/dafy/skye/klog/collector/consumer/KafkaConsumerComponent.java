package com.dafy.skye.klog.collector.consumer;

import com.dafy.skye.klog.collector.AbstractCollectorComponent;
import com.dafy.skye.klog.collector.CollectorConfig;
import com.dafy.skye.klog.core.JavaDeserializer;
import com.dafy.skye.klog.core.logback.KLogEvent;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Caedmon on 2017/4/14.
 */
public class KafkaConsumerComponent extends AbstractCollectorComponent implements ConsumerComponent{
    private KafkaConsumer<String,Object> kafkaConsumer;
    private volatile AtomicBoolean started=new AtomicBoolean(false);
    private static final Logger log= LoggerFactory.getLogger(KafkaConsumerComponent.class);
    private TopicPartition topicPartition;
    private long currentOffset;
    private Properties kafkaProperties;
    private long pollInterval;
    public KafkaConsumerComponent(long pollInterval,Properties kafkaProperties){
        this.pollInterval=pollInterval;
        this.kafkaProperties=kafkaProperties;
    }
    @Override
    public void start() {
        if(!started.get()){
            kafkaConsumer=new KafkaConsumer<>(this.kafkaProperties,new StringDeserializer(),new JavaDeserializer());
            TopicPartition topicPartition=new TopicPartition(collectorConfig.getTopic(),collectorConfig.getPartition());
            kafkaConsumer.assign(Collections.singleton(topicPartition));
            this.topicPartition=topicPartition;
            started.set(true);
            log.info("Consumer started:topicName={},partition={},pullInterval={}",
                    collectorConfig.getTopic(),collectorConfig.getPartition(),
                    this.pollInterval);
        }else{
            log.warn("KafkaConsumer has already started");
        }
    }

    @Override
    public void stop() {
        if(started.get()){
            kafkaConsumer.close();
            started.set(false);
        }
    }
    @Override
    public void seek(long offset) {
        kafkaConsumer.seek(this.topicPartition,offset);
        this.currentOffset=offset;
    }

    @Override
    public long currentOffset() {
        return this.currentOffset;
    }

    @Override
    public PollResult poll() {
        final long beforePollOffset=this.currentOffset;
        //从kafka pull 消息
        PollResult result=null;
        try{
            ConsumerRecords<String, Object> records = kafkaConsumer.poll(this.pollInterval);
            result=new PollResult(records.count());
            for (ConsumerRecord<String, Object> record : records) {
                //如果消息offset小于start则忽略
                if(record.offset()<=currentOffset){
                    continue;
                }
                KLogEvent event=(KLogEvent) record.value();
                result.addKLog(event);
                result.setEndOffset(record.offset());
                this.currentOffset=result.getEndOffset();
            }
        }catch (Throwable e){
            log.error("Poll event error",e);
            //回滚offset
            this.currentOffset=beforePollOffset;
            throw e;
        }
        return result;
    }

    @Override
    public void commit(long offset) {
        kafkaConsumer.commitAsync(new OffsetCommitCallback() {
            @Override
            public void onComplete(Map<TopicPartition, OffsetAndMetadata> offsets, Exception exception) {
                if(exception!=null){
                    exception.printStackTrace();
                    log.error("Commit com.dafy.skye.klog.collector.offset error ",exception);
                }
            }
        });
    }
}
