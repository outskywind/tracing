package com.dafy.kafka;


import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * 使用kafka consumer group 管理消费者offset,运行时将会判断上次的消费者offset保存下来
 * 确保1.不会重复消费，2.不会跳跃offset丢失数据
 * 对于1.判断最后一个record的offset是否小于当前offset，是则丢弃；否则倒序遍历 retrieve
 * 对于2.判断第一个获取的record的offset是否大于当前offset
 * //这里没有事务，因此可能还是会重复消费，二阶段提交模式先提交再ACK，那么就是会重复
 * kafka 保证的是至少一次，not exactly once
 */
public class KafkaConsumer {

    private final Logger log= LoggerFactory.getLogger(KafkaConsumer.class);

    private KafkaConfigurationProperties kafkaConfig;

    private MessageConsumer messageConsumer;

    private ExecutorService executor ;

    private volatile AtomicBoolean started=new AtomicBoolean(false);

    private volatile AtomicBoolean ISRUN = new AtomicBoolean(true);


    public void start() {
        if(started.compareAndSet(false,true)){
            log.info("Start KafkaCollector with group.id  {}",kafkaConfig.getProperties().getProperty("group.id"));
            org.apache.kafka.clients.consumer.KafkaConsumer consumer = new org.apache.kafka.clients.consumer.KafkaConsumer(kafkaConfig.getProperties());
            List<PartitionInfo> partitions = consumer.partitionsFor(kafkaConfig.getTopic());

            List<org.apache.kafka.clients.consumer.KafkaConsumer> consumers = new ArrayList<>();
            consumers.add(consumer);
            for(int i=1;i<partitions.size();i++){
                org.apache.kafka.clients.consumer.KafkaConsumer nextConsumer = new org.apache.kafka.clients.consumer.KafkaConsumer(kafkaConfig.getProperties());
                consumers.add(nextConsumer);
            }
            runConsumer(consumers,kafkaConfig.getTopic());
        }
    }

    void runConsumer(List<org.apache.kafka.clients.consumer.KafkaConsumer> consumers,String topic){
        executor = Executors.newFixedThreadPool(consumers.size());
        final Map<String,OffsetAndMetadata> initialOffsetMap = new HashMap<>();
        //初始化offset
        for(final org.apache.kafka.clients.consumer.KafkaConsumer consumer:consumers){
            consumer.subscribe(Arrays.asList(topic), new ConsumerRebalanceListener(){
                @Override
                public void onPartitionsRevoked(Collection<TopicPartition> partitions) {

                }
                //在第一次poll实际发送到kafka broker之前回调
                @Override
                public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
                    for(TopicPartition tp : partitions){
                        //这里指的是获取上一次提交的offset，字面意思来看正是我们想要的
                        //但是不知是否会被预先重置掉？
                        OffsetAndMetadata _offset  = consumer.committed(tp);
                        initialOffsetMap.put(tp.topic()+tp.partition(),_offset);
                    }
                }
            });
        }
        //运行consumer
        for(final org.apache.kafka.clients.consumer.KafkaConsumer consumer:consumers){
            executor.submit(new Runnable() {
                Map<String,Boolean> needValidateOffset = new HashMap<>();
                @Override
                public void run() {
                    try {
                        while (ISRUN.get()){
                            ConsumerRecords<String, byte[]> records = consumer.poll(1000);
                            if(records.isEmpty()){
                                continue;
                            }
                            List<byte[]> list=new ArrayList<>(records.count());
                            //optimize the retrieve method
                            Map<String,Integer> topicPartitionRetrieveIdx=new HashMap<>();

                            for (ConsumerRecord<String, byte[]> record : records) {
                                String tpStr = record.topic()+record.partition();
                                boolean needValidate =  needValidateOffset.get(tpStr)==null?true:needValidateOffset.get(tpStr);
                                //如果是没有初始化过offset不用校验
                                OffsetAndMetadata initialOffset = initialOffsetMap.get(tpStr);
                                if(initialOffset==null){
                                    needValidateOffset.put(tpStr,false);
                                    byte[] content = record.value();
                                    if(content!=null&&content.length>0){
                                        list.add(content);
                                    }
                                    continue;
                                }
                                if(needValidate){
                                    int idx =  topicPartitionRetrieveIdx.get(tpStr)==null?0:topicPartitionRetrieveIdx.get(tpStr)+1;
                                    topicPartitionRetrieveIdx.put(tpStr,idx);
                                    //当needValidateOffset 为true时获取第一个判断
                                    //说明已经产生的offset跳跃，需要重置
                                    if(idx==0 && record.offset()>initialOffset.offset() ){
                                        consumer.seek(new TopicPartition(record.topic(),record.partition()),initialOffset.offset());
                                        log.warn("kafka offset reset detected {},and seek back to last committed offset {},good luck... ",record.offset(),initialOffset.offset());
                                        break;
                                    }
                                    //如果消息offset小于initialOffset则忽略
                                    if (record.offset() < initialOffset.offset()) {
                                        continue;
                                    }
                                    //符合预期offset,关闭校验
                                    else if(record.offset() == initialOffset.offset()){
                                        needValidateOffset.put(tpStr,false);
                                    }
                                    byte[] content = record.value();
                                    if(content!=null&&content.length>0){
                                        list.add(content);
                                    }
                                }else{
                                    list.add(record.value());
                                }
                            }
                            //事务失败不提交
                            if(!messageConsumer.accept(list)) continue;
                            consumer.commitAsync(new OffsetCommitCallback() {
                                @Override
                                public void onComplete(Map<TopicPartition, OffsetAndMetadata> offsets, Exception exception) {
                                    if(exception!=null){
                                        exception.printStackTrace();
                                        log.error("Commit offset error ",exception);
                                    }
                                }
                            });
                        }
                    } catch (Throwable e) {
                        log.error("poll event error,collector shutting down", e);
                        consumer.close();
                        throw e;
                    }
                }
            });
        }
    }

    public void stop() {
        ISRUN.set(false);
    }

    public KafkaConfigurationProperties getKafkaConfig() {
        return kafkaConfig;
    }

    public void setKafkaConfig(KafkaConfigurationProperties kafkaConfig) {
        this.kafkaConfig = kafkaConfig;
    }

    public MessageConsumer getMessageConsumer() {
        return messageConsumer;
    }

    public void setMessageConsumer(MessageConsumer messageConsumer) {
        this.messageConsumer = messageConsumer;
    }
}
