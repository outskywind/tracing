package com.dafy.skye.klog.collector.consumer;

import com.dafy.skye.klog.collector.Collector;
import com.dafy.skye.klog.collector.offset.OffsetComponent;
import com.dafy.skye.klog.core.JavaDeserializer;
import com.dafy.skye.klog.core.logback.KLogEvent;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Caedmon on 2017/4/25.
 */
public class KafkaCollector {
    private KafkaCollectorConfig kafkaCollectorConfig;
    private Collector delegate;
    private List<KafkaConsumer<String,Object>> kafkaConsumers;
    private ExecutorService threadPool;
    private OffsetComponent offsetComponent;
    private Map<Integer,Long> partitionOffsetMap=new HashMap<>();
    public KafkaCollector(KafkaCollectorConfig kafkaCollectorConfig,
                          Collector delegate, OffsetComponent offsetComponent){
        this.kafkaCollectorConfig=kafkaCollectorConfig;
        this.delegate=delegate;
        this.offsetComponent=offsetComponent;
    }
    private volatile AtomicBoolean started=new AtomicBoolean(false);
    private static final Logger log= LoggerFactory.getLogger(KafkaConsumerComponent.class);
    public void start() {
        if(!started.get()){
            final int partition=kafkaCollectorConfig.getPartition();
            threadPool=partition<=1?Executors.newSingleThreadExecutor():Executors.newFixedThreadPool(partition);
            for(int i=0;i<partition;i++){
                KafkaConsumer kafkaConsumer=new KafkaConsumer<>(kafkaCollectorConfig.getProperties(),
                        new StringDeserializer(),new JavaDeserializer());
                TopicPartition topicPartition=new TopicPartition(
                        kafkaCollectorConfig.getTopic(),i);
                kafkaConsumer.assign(Collections.singleton(topicPartition));
                threadPool.execute(buildTask(topicPartition,kafkaConsumer));
            }
            started.set(true);
        }else{
            log.warn("KafkaConsumer has already started");
        }
    }
    public void stop() {
        if(started.get()){
            if(kafkaConsumers!=null&&!kafkaConsumers.isEmpty()){
                for(KafkaConsumer kafkaConsumer:kafkaConsumers){
                    kafkaConsumer.close();
                }
            }
            started.set(false);
        }
    }
    public Runnable buildTask(final TopicPartition partition, final KafkaConsumer kafkaConsumer){
        Runnable task=new Runnable() {
            @Override
            public void run() {
                long offset = offsetComponent.getOffset();
                kafkaConsumer.seek(partition, offset);
                partitionOffsetMap.put(partition.partition(), offset);
                long currentOffset = offset;
                final long beforePollOffset=offset;
                final long pollInterval=KafkaCollector.this.kafkaCollectorConfig.getPollInterval();
                try {
                    ConsumerRecords<String, Object> records = kafkaConsumer.poll(pollInterval);
                    for (ConsumerRecord<String, Object> record : records) {
                        //如果消息offset小于start则忽略
                        long lastOffset=record.offset();
                        if (lastOffset <= currentOffset) {
                            continue;
                        }
                        KLogEvent event = (KLogEvent) record.value();
                        KafkaCollector.this.delegate.acceptEvent(event);
                        if(lastOffset>offset){
                            currentOffset=lastOffset;
                            KafkaCollector.this.partitionOffsetMap.put(partition.partition(),record.offset());
                            kafkaConsumer.commitAsync(new OffsetCommitCallback() {
                                @Override
                                public void onComplete(Map<TopicPartition, OffsetAndMetadata> offsets, Exception exception) {
                                    if(exception!=null){
                                        exception.printStackTrace();
                                        log.error("Commit offset error ",exception);
                                    }
                                }
                            });
                        }
                    }
                } catch (Throwable e) {
                    log.error("Poll event error", e);
                    //回滚offset
                    partitionOffsetMap.put(partition.partition(), beforePollOffset);
                    kafkaConsumer.close();
                    throw e;
                }

            }
        };
        return task;

    }

}
