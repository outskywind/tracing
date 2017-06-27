package com.dafy.skye.log.server.kafka;

import com.dafy.skye.log.server.autoconfig.KafkaCollectorConfigProperties;
import com.dafy.skye.log.server.collector.CollectorComponent;
import com.dafy.skye.log.server.collector.CollectorDelegate;
import com.dafy.skye.log.server.kafka.offset.OffsetComponent;
import com.dafy.skye.log.core.SkyeLogEventCodec;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Caedmon on 2017/4/25.
 * Kafka 收集器,日志通过SkyeLogKafkaAppender 写入kafka
 * 通过KafkaCollector 消费后写入存储组件
 */
public class KafkaCollector implements CollectorComponent {
    private KafkaCollectorConfigProperties kafkaCollectorConfig;
    private CollectorDelegate delegate;
    private List<KafkaConsumer<String,byte[]>> kafkaConsumers=new ArrayList<>();
    private ExecutorService threadPool;
    //消费指针管理组件
    private OffsetComponent offsetComponent;
    //每个分区对应的消费指针
    private Map<Integer,Long> partitionOffsetMap=new HashMap<>();
    public KafkaCollector(KafkaCollectorConfigProperties kafkaCollectorConfig,
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
            KafkaConsumer leaderConsumer=new KafkaConsumer(kafkaCollectorConfig.getProperties(),
                    new StringDeserializer(),new ByteArrayDeserializer());
            List<PartitionInfo> partitionInfos=leaderConsumer.partitionsFor(kafkaCollectorConfig.getTopic());
            for(PartitionInfo info:partitionInfos){
                TopicPartition topicPartition=new TopicPartition(
                        info.topic(),info.partition());
                KafkaConsumer childConsumer=new KafkaConsumer(kafkaCollectorConfig.getProperties(),
                        new StringDeserializer(),new ByteArrayDeserializer());
                childConsumer.assign(Collections.singleton(topicPartition));
                Runnable task=buildTask(topicPartition,childConsumer);
                threadPool.execute(task);
                kafkaConsumers.add(childConsumer);
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
                        List<byte[]> list=new ArrayList<>(records.count());
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
                            if(content!=null&&content.length>0){
                                list.add(content);
                            }
                        }
                        KafkaCollector.this.delegate.acceptEvents(list, SkyeLogEventCodec.DEFAULT);
                        //最后消费的offset如果大于当前offset则更新缓存并提交
                        if(lastOffset>=currentOffset){
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
