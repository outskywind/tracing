package com.dafy.skye.klog.collector;

import com.dafy.skye.klog.collector.consumer.KafkaConsumerComponent;
import com.dafy.skye.klog.collector.offset.redis.RedisConfig;
import com.dafy.skye.klog.collector.offset.redis.RedisOffsetComponent;
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
            CollectorConfig collectorConfig=CollectorConfig.Builder.create()
                    .build(properties);
            collectorConfig.setPartition(partition);
            DefaultCollector.Builder builder= DefaultCollector.Builder.create();
            builder.collectorConfig(collectorConfig);
            builder.consumerComponent(new KafkaConsumerComponent(1000L,properties));
            RollingFileStorageConfig rollingFileStorageConfig=RollingFileStorageConfig.Builder.create()
                    .build(properties);
            builder.storageComponent(new RollingFileStorage(rollingFileStorageConfig));
            RedisConfig redisConfig=RedisConfig.Builder.create().build(properties);
            builder.offsetComponent(new RedisOffsetComponent(redisConfig));
            DefaultCollector collector=builder.build();
            log.info("Create collector:{}",collector.toString());
            executorService.execute(collector);
        }
    }
}
