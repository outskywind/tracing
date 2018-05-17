package com.dafy.skye.log.server.autoconfig;

import com.dafy.kafka.KafkaConfigurationProperties;
import com.dafy.kafka.KafkaConsumer;
import com.dafy.skye.log.server.collector.CollectorDelegate;
import com.dafy.skye.log.server.collector.filter.CollectFilter;
import com.dafy.skye.log.server.collector.filter.LogLevelFilter;
import com.dafy.skye.log.server.kafka.SkyeLogMessageConsumer;
import com.dafy.skye.log.server.metrics.MemoryCollectorMetrics;
import com.dafy.skye.log.server.storage.StorageComponent;
import com.dafy.skye.log.server.storage.elasticsearch.ElasticSearchStorage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Created by Caedmon on 2017/4/24.
 */
@Configuration
@EnableConfigurationProperties({LogStorageESConfigProperties.class})
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
    CollectorDelegate collectorDelegate(StorageComponent storageComponent, List<CollectFilter> filters){
        CollectorDelegate delegate=new CollectorDelegate(storageComponent,new MemoryCollectorMetrics());
        return delegate;
    }

    @Bean
    CollectFilter logLevelFilter(){
        LogLevelFilter filter = new LogLevelFilter();
        filter.setOrder(0);
        return filter;
    }

    @Bean
    SkyeLogMessageConsumer skyeLogMessageConsumer(CollectorDelegate delegate){
        SkyeLogMessageConsumer skyeLogMessageConsumer = new SkyeLogMessageConsumer();
        skyeLogMessageConsumer.setDelegate(delegate);
        return skyeLogMessageConsumer;
    }

    @ConfigurationProperties("skye.log.server.kafka")
    @Bean("skye.log.kafkaConfigurationProperties")
    KafkaConfigurationProperties kafkaConfigurationProperties(){
        return new KafkaConfigurationProperties();
    }

    @Bean(initMethod = "start")
    public KafkaConsumer kafkaConsumer(SkyeLogMessageConsumer qpsMessageConsumer){
        KafkaConsumer consumer = new KafkaConsumer();
        consumer.setKafkaConfig(kafkaConfigurationProperties());
        consumer.setMessageConsumer(qpsMessageConsumer);
        return consumer;
    }

}
