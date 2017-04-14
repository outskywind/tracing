package com.dafy.skye.klog.collector.consumer;

import com.dafy.skye.klog.core.logback.KLogEvent;
import com.google.common.collect.Lists;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Caedmon on 2017/4/14.
 */
public class KafkaConsumerComponent implements ConsumerComponent {
    private ConsumerConfig consumerConfig;
    private KafkaConsumer<String,Object> kafkaConsumer;
    private volatile AtomicBoolean started=new AtomicBoolean(false);
    private static final Logger log= LoggerFactory.getLogger(KafkaConsumerComponent.class);
    private TopicPartition topicPartition;
    public KafkaConsumerComponent(ConsumerConfig consumerConfig){
        this.consumerConfig=consumerConfig;
    }

    @Override
    public boolean start() {
        if(!started.get()){
            kafkaConsumer=new KafkaConsumer<>(consumerConfig.getProperties());
            TopicPartition topicPartition=new TopicPartition(consumerConfig.getTopic(),consumerConfig.getPartition();
            kafkaConsumer.assign(Collections.singleton(topicPartition));
            this.topicPartition=topicPartition;
            started.set(true);
            log.info("Consumer started:topicName={},partition={},pullInterval={}",
                    consumerConfig.getTopic(),consumerConfig.getPartition(),
                    consumerConfig.getPullInterval());
        }
        return started.get();
    }

    @Override
    public void setConsumerConfig(ConsumerConfig consumerConfig) {
        this.consumerConfig=consumerConfig;
    }

    @Override
    public void seek(long offset) {
        kafkaConsumer.seek(this.topicPartition,offset);
    }

    @Override
    public List<KLogEvent> poll() {
        return null;
    }

    @Override
    public void commit(long offset) {

    }
}
