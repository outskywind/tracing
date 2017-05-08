package com.dafy.skye.log.collector.kafka;

import com.dafy.skye.log.collector.CollectorDelegate;
import com.dafy.skye.log.collector.CollectorComponent;
import com.dafy.skye.log.collector.kafka.offset.OffsetComponent;
import com.dafy.skye.log.core.SkyeLogDeserializer;
import com.dafy.skye.log.core.logback.SkyeLogEvent;
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
public class KafkaCollector implements CollectorComponent {
    private KafkaCollectorConfig kafkaCollectorConfig;
    private CollectorDelegate delegate;
    private List<KafkaConsumer<String,byte[]>> kafkaConsumers;
    private ExecutorService threadPool;
    private OffsetComponent offsetComponent;
    private Map<Integer,Long> partitionOffsetMap=new HashMap<>();
    public KafkaCollector(KafkaCollectorConfig kafkaCollectorConfig,
                          CollectorDelegate delegate, OffsetComponent offsetComponent){
        this.kafkaCollectorConfig=kafkaCollectorConfig;
        this.delegate=delegate;
        this.offsetComponent=offsetComponent;
    }
    private volatile AtomicBoolean started=new AtomicBoolean(false);
    private static final Logger log= LoggerFactory.getLogger(KafkaCollector.class);
    @Override
    public void start() {
        if(!started.get()){
            final int partition=kafkaCollectorConfig.getPartition();
            threadPool=partition<=1?Executors.newSingleThreadExecutor():Executors.newFixedThreadPool(partition);
            for(int i=0;i<partition;i++){
                KafkaConsumer kafkaConsumer=new KafkaConsumer(kafkaCollectorConfig.getProperties(),
                        new StringDeserializer(),new SkyeLogDeserializer());
                TopicPartition topicPartition=new TopicPartition(
                        kafkaCollectorConfig.getTopic(),i);
                kafkaConsumer.assign(Collections.singleton(topicPartition));
                threadPool.execute(buildTask(topicPartition,kafkaConsumer));
            }
            started.set(true);
            log.info("KafkaCollector started:config={}",this.kafkaCollectorConfig);
        }else{
            log.warn("KafkaCollector has already started");
        }
    }
    @Override
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
    public Runnable buildTask(final TopicPartition topicPartition, final KafkaConsumer kafkaConsumer){
        final int partition=topicPartition.partition();
        final String partitionKey=this.kafkaCollectorConfig.getTopic()+"."
                +this.kafkaCollectorConfig.getGroupId()
                +"."+partition;
        Runnable task=new Runnable() {
            @Override
            public void run() {
                //从redis读取offset
                long currentOffset = offsetComponent.getOffset(partitionKey);
                kafkaConsumer.seek(topicPartition, currentOffset);
                partitionOffsetMap.put(partition, currentOffset);
                //poll之前的offset
                final long beforePollOffset=currentOffset;
                final long pollInterval=KafkaCollector.this.kafkaCollectorConfig.getPollInterval();
                try {
                    while (true){
                        ConsumerRecords<String, byte[]> records = kafkaConsumer.poll(pollInterval);
                        if(records.isEmpty()){
                            continue;
                        }
                        List<SkyeLogEvent> list=new ArrayList<>(records.count());
                        //最新offset
                        long lastOffset=currentOffset;
                        for (ConsumerRecord<String, byte[]> record : records) {
                            //如果消息offset小于start则忽略
                            if (record.offset() <= currentOffset) {
                                continue;
                            }
                            //更新最新offset
                            lastOffset=record.offset();
                            byte[] content = record.value();
                            SkyeLogDeserializer deserializer=new SkyeLogDeserializer();
                            SkyeLogEvent event=deserializer.deserialize(null,content);
                            if(event==null){
                                log.error("Deserialize SkyeLogEvent error,event is null");
                                continue;
                            }
                            list.add(event);
                        }
                        KafkaCollector.this.delegate.acceptEvents(list);
                        //最后消费的offset如果大于当前offset则更新缓存并提交
                        if(lastOffset>currentOffset){
                            currentOffset=lastOffset;
                            KafkaCollector.this.partitionOffsetMap.put(partition,lastOffset);
                            kafkaConsumer.commitAsync(new OffsetCommitCallback() {
                                @Override
                                public void onComplete(Map<TopicPartition, OffsetAndMetadata> offsets, Exception exception) {
                                    if(exception!=null){
                                        exception.printStackTrace();
                                        log.error("Commit offset error ",exception);
                                    }
                                }
                            });
                            offsetComponent.setOffset(partitionKey,lastOffset);
                            log.debug("Poll events success:beforePollOffset={},afterPollOffset={}",beforePollOffset,currentOffset);
                        }
                    }

                } catch (Throwable e) {
                    log.error("Poll event error,collector will shutdown", e);
                    //回滚offset
                    partitionOffsetMap.put(partition, beforePollOffset);
                    kafkaConsumer.close();
                    throw e;
                }

            }
        };
        return task;

    }

}