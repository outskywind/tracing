package com.dafy.skye.klog.collector;

import com.dafy.skye.klog.collector.consumer.KafkaConsumerComponent;
import com.dafy.skye.klog.collector.offset.redis.RedisConfig;
import com.dafy.skye.klog.collector.offset.redis.RedisOffsetComponent;
import com.dafy.skye.klog.collector.storage.rolling.RollingFileStorage;
import com.dafy.skye.klog.collector.storage.rolling.RollingFileStorageConfig;

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Caedmon on 2017/4/14.
 */
public class CollectorController {
    private Properties properties;
    private ExecutorService executorService;
    public CollectorController(Properties properties) throws Exception{
        this.properties=properties;
    }
    public void start() throws Exception{
        String consumerSizeValue=properties.getProperty("skye-klog-collector.parallel");
        int consumerSize=1;
        if(consumerSizeValue!=null){
            consumerSize=Integer.parseInt(consumerSizeValue);
        }
        CollectorConfig collectorConfig=CollectorConfig.Builder.create()
                .build(properties);
        executorService= Executors.newFixedThreadPool(consumerSize);
        for(int i=0;i<consumerSize;i++){
            DefaultCollector.Builder builder= DefaultCollector.Builder.create();
            builder.collectorConfig(collectorConfig);
            builder.consumerComponent(new KafkaConsumerComponent(1000L,properties));
            RollingFileStorageConfig rollingFileStorageConfig=RollingFileStorageConfig.Builder.create()
                    .build(properties);
            builder.storageComponent(new RollingFileStorage(rollingFileStorageConfig));
            RedisConfig redisConfig=RedisConfig.Builder.create().build(properties);
            builder.offsetComponent(new RedisOffsetComponent(redisConfig));
            DefaultCollector collector=builder.build();
            executorService.execute(collector);
        }
    }
}
