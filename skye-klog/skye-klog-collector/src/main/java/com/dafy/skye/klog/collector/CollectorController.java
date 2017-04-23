package com.dafy.skye.klog.collector;

import com.dafy.skye.klog.collector.consumer.KafkaConsumerComponent;
import com.dafy.skye.klog.collector.offset.redis.RedisConfig;
import com.dafy.skye.klog.collector.offset.redis.RedisOffsetComponent;
import com.dafy.skye.klog.collector.storage.cassandra.CassandraConfigProperties;
import com.dafy.skye.klog.collector.storage.cassandra.CassandraStorage;
import com.dafy.skye.klog.collector.storage.rolling.RollingFileStorage;
import com.dafy.skye.klog.collector.storage.rolling.RollingFileStorageConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Caedmon on 2017/4/14.
 */
public class CollectorController {
    private Properties properties;
    private ExecutorService executorService;
    private static final Logger log= LoggerFactory.getLogger(CollectorController.class);
    public CollectorController(Properties properties) throws Exception{
        this.properties=properties;
    }
    public void start() throws Exception{
        int parallel=Integer.parseInt(properties.getProperty("skye-klog-collector.parallel","4"));

        executorService= Executors.newFixedThreadPool(parallel);
        for(int partition=0;partition<parallel;partition++){
            CollectorPartitionConfig collectorPartitionConfig = CollectorPartitionConfig.Builder.create()
                    .build(properties);
            collectorPartitionConfig.setPartition(partition);
            DefaultPartitionCollector.Builder builder= DefaultPartitionCollector.Builder.create();
            builder.collectorConfig(collectorPartitionConfig);
            builder.consumerComponent(new KafkaConsumerComponent(1000L,properties));
//            RollingFileStorageConfig rollingFileStorageConfig=RollingFileStorageConfig.Builder.create()
//                    .build(properties);
            CassandraConfigProperties cassandraConfigProperties=new CassandraConfigProperties();
            builder.storageComponent(new CassandraStorage(cassandraConfigProperties));
            RedisConfig redisConfig=RedisConfig.Builder.create().build(properties);
            builder.offsetComponent(new RedisOffsetComponent(redisConfig));
            DefaultPartitionCollector collector=builder.build();
            log.info("Create collector:{}",collector.toString());
            executorService.execute(collector);
        }
    }
}
