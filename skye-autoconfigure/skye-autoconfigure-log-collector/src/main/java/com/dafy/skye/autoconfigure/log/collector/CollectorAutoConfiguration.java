package com.dafy.skye.autoconfigure.log.collector;

import com.dafy.skye.log.collector.CollectorDelegate;
import com.dafy.skye.log.collector.kafka.KafkaCollector;
import com.dafy.skye.log.collector.kafka.KafkaCollectorConfig;
import com.dafy.skye.log.collector.kafka.offset.OffsetComponent;
import com.dafy.skye.log.collector.kafka.offset.redis.RedisOffsetComponent;
import com.dafy.skye.log.collector.storage.StorageComponent;
import com.dafy.skye.log.collector.storage.cassandra.CassandraConfig;
import com.dafy.skye.log.collector.storage.cassandra.CassandraStorage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;

/**
 * Created by Caedmon on 2017/4/24.
 */
@Configuration
@EnableConfigurationProperties({
CassandraConfigProperties.class,
KafkaCollectorConfigProperties.class})
public class CollectorAutoConfiguration {

    @Bean(initMethod = "start")
    @ConditionalOnMissingBean(StorageComponent.class)
    @ConditionalOnProperty(value = "skye.log.collector.storage.type",havingValue = "cassandra")
    StorageComponent storageComponent(CassandraConfigProperties cassandraConfigProperties){
        CassandraConfig cassandraConfig=cassandraConfigProperties.buildCassandraConfig();
        return new CassandraStorage(cassandraConfig);
    }

    @Bean(initMethod = "start")
    @ConditionalOnBean(StorageComponent.class)
    CollectorDelegate collectorDelegate(StorageComponent storageComponent){
        CollectorDelegate delegate=new CollectorDelegate(storageComponent,null);
        return delegate;
    }

    @Bean(initMethod = "start")
    @ConditionalOnProperty(name = "skye.log.collector.kafka.offset.type",havingValue = "redis")
    OffsetComponent offsetComponent(JedisPool jedisPool){
        OffsetComponent offsetComponent=new RedisOffsetComponent(jedisPool);
        return offsetComponent;
    }
    @Bean(initMethod = "start")
    @ConditionalOnBean({OffsetComponent.class,CollectorDelegate.class})
    KafkaCollector kafkaCollector(CollectorDelegate delegate,
                                         KafkaCollectorConfigProperties kafkaCollectorConfigProperties,
                                         OffsetComponent offsetComponent){
        KafkaCollectorConfig kafkaCollectorConfig=kafkaCollectorConfigProperties.buildKafkaCollectorConfig();
        KafkaCollector kafkaCollector=new KafkaCollector(kafkaCollectorConfig,delegate,offsetComponent);
        return kafkaCollector;
    }
}
