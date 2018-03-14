package com.dafy.skye.log.server.autoconfig;

import com.dafy.skye.log.server.collector.CollectorDelegate;
import com.dafy.skye.log.server.kafka.KafkaCollectorV2;
import com.dafy.skye.log.server.kafka.offset.OffsetComponent;
import com.dafy.skye.log.server.metrics.MemoryCollectorMetrics;
import com.dafy.skye.log.server.storage.StorageComponent;
import com.dafy.skye.log.server.storage.elasticsearch.ElasticSearchStorage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by Caedmon on 2017/4/24.
 */
@Configuration
@EnableConfigurationProperties({
KafkaCollectorConfigProperties.class,
LogStorageESConfigProperties.class})
public class LogServerAutoConfiguration {

    @ConditionalOnMissingBean(StorageComponent.class)
    @ConditionalOnProperty(value = "skye.log.server.storage.type",havingValue = "elasticsearch")
    @Bean(initMethod = "start")
    ElasticSearchStorage elasticSearchStorage(LogStorageESConfigProperties properties){
        ElasticSearchStorage storage=new ElasticSearchStorage(properties);
        return storage;
    }
    @ConditionalOnBean(StorageComponent.class)
    @Bean
    CollectorDelegate collectorDelegate(StorageComponent storageComponent){
        CollectorDelegate delegate=new CollectorDelegate(storageComponent,new MemoryCollectorMetrics());
        return delegate;
    }

    /*@Bean(initMethod = "start")
    @ConditionalOnProperty(name = "skye.log.server.kafka.offset.type",havingValue = "redis")
    OffsetComponent offsetComponent(JedisPool jedisPool){
        OffsetComponent offsetComponent=new RedisOffsetComponent(jedisPool);
        return offsetComponent;
    }*/

    /*@ConditionalOnBean({OffsetComponent.class,CollectorDelegate.class})
    @Bean(initMethod = "start")
    KafkaCollector kafkaCollector(CollectorDelegate delegate,
                                         KafkaCollectorConfigProperties kafkaCollectorConfigProperties,
                                         OffsetComponent offsetComponent){
        KafkaCollector kafkaCollector=new KafkaCollector(kafkaCollectorConfigProperties,delegate,offsetComponent);
        return kafkaCollector;
    }*/

    @ConditionalOnBean({OffsetComponent.class,CollectorDelegate.class})
    @Bean(initMethod = "start")
    KafkaCollectorV2 kafkaCollectorV2(){
        return new KafkaCollectorV2();
    }

}
