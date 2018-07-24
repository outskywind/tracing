package com.dafy.skye.zipkin.config.druid;

import com.dafy.kafka.KafkaConfigurationProperties;
import com.dafy.kafka.KafkaConsumer;
import com.dafy.skye.druid.consumer.DruidMessageConsumer;
import com.dafy.skye.druid.entity.SkyeMetric;
import com.dafy.skye.druid.realtime.DruidRealtimeSender;
import com.dafy.skye.druid.rest.RestDruidClient;
import com.dafy.skye.zk.ZkServiceDiscovery;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.metamx.tranquility.config.PropertiesBasedConfig;
import com.metamx.tranquility.config.TranquilityConfig;
import com.metamx.tranquility.druid.DruidBeams;
import com.metamx.tranquility.partition.HashCodePartitioner;
import com.metamx.tranquility.partition.Partitioner;
import com.metamx.tranquility.tranquilizer.Tranquilizer;
import com.metamx.tranquility.typeclass.JsonWriter;
import com.metamx.tranquility.typeclass.ObjectWriter;
import com.metamx.tranquility.typeclass.Timestamper;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Dsl;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Created by quanchengyun on 2018/5/15.
 */
@Configuration
@Import(ZkConfiguration.class)
@ConditionalOnProperty(prefix="druid",name={"zk.host"})
public class ZipkinDruidStorageAutoConfiguration {

    @ConditionalOnMissingBean(name="zipkin.restDruidClient")
    @Bean("zipkin.restDruidClient")
    @ConditionalOnBean(ZkServiceDiscovery.class)
    public RestDruidClient restDruidClient(AsyncHttpClient httpClient, ZkServiceDiscovery hostDiscovery,ObjectMapper json){
        RestDruidClient client =  new RestDruidClient();
        client.setHttpClient(httpClient);
        client.setHostDiscovery(hostDiscovery);
        client.setJson(json);
        return client;
    }

    @ConditionalOnMissingBean(name="zipkin.asyncHttpClient")
    @Bean("zipkin.asyncHttpClient")
    public AsyncHttpClient asyncHttpClient(){
        AsyncHttpClient client = Dsl.asyncHttpClient();
        return client;
    }


    @Bean
    public Timestamper<SkyeMetric> initTimestamper(){
        return new Timestamper<SkyeMetric>(){
            @Override
            public DateTime timestamp(SkyeMetric skyeMetric) {
                DateTime dt = new DateTime();
                dt.withMillis(skyeMetric.getTimestamp());
                return dt;
            }
        };
    }
    /**
     * 使用一致hash分区
     * @return
     */
    @Bean
    public Partitioner<SkyeMetric> initPartitioner(){
        return new HashCodePartitioner<SkyeMetric>();
    }

    @Bean
    public ObjectWriter<SkyeMetric> initObjectWriter(final ObjectMapper objectMapper){
        return new JsonWriter<SkyeMetric>() {
            @Override
            public void viaJsonGenerator(SkyeMetric skyeMetric, JsonGenerator jsonGenerator) {
                try {
                    String a = objectMapper.writeValueAsString(skyeMetric);
                    objectMapper.writeValue(jsonGenerator, skyeMetric);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    @ConditionalOnMissingBean(name = "zipkin.druidRealtimeSender")
    @Bean("zipkin.druidRealtimeSender")
    public DruidRealtimeSender druidRealtimeSender(Timestamper<SkyeMetric> timestamper, ObjectWriter<SkyeMetric> objectWriter, Partitioner<SkyeMetric> partitioner){
        DruidRealtimeSender instance = new DruidRealtimeSender();

        InputStream ins = Thread.currentThread().getContextClassLoader().getResourceAsStream("tranquility.yml");
        TranquilityConfig<PropertiesBasedConfig> config = TranquilityConfig.read(ins);
        List<String> dataSources = config.getDataSources();
        for(String dataSource: dataSources){
            Tranquilizer<SkyeMetric> sender = DruidBeams.fromConfig(config.getDataSource(dataSource),timestamper,objectWriter).partitioner(partitioner)
                    .buildTranquilizer(config.getDataSource(dataSource).tranquilizerBuilder());
            sender.start();
            instance.add(dataSource,sender);
        }
        try{
            ins.close();
        }catch (Exception e){
            //.nothing
        }
        return instance;
    }

    @ConditionalOnMissingBean(name="zipkin.druidKafkaConsumer")
    @Bean("zipkin.druidKafkaConsumer")
    public DruidMessageConsumer druidMessageConsumer(@Qualifier("zipkin.druidRealtimeSender") DruidRealtimeSender sender){
        DruidMessageConsumer instance = new DruidMessageConsumer();
        instance.setSender(sender);
        return instance;
    }


    @Bean(name="zipkin.druidKafkaConfig")
    @ConfigurationProperties(prefix="druid.kafka")
    public KafkaConfigurationProperties  druidKafkaConfig(){
        return new KafkaConfigurationProperties();
    }

    @Bean(initMethod ="start",destroyMethod ="stop")
    @ConditionalOnBean(DruidMessageConsumer.class)
    public KafkaConsumer kafkaConsumer(@Qualifier("zipkin.druidKafkaConfig") KafkaConfigurationProperties kafkaConfig,DruidMessageConsumer druidMessageConsumer){
        KafkaConsumer consumer = new KafkaConsumer();
        consumer.setKafkaConfig(kafkaConfig);
        consumer.setMessageConsumer(druidMessageConsumer);
        return consumer;
    }


}
