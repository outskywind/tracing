package com.dafy.skye.storm.config;

import com.dafy.skye.components.SkyeRecordTranslator;
import com.dafy.skye.components.SpanCodecComponent;
import com.dafy.skye.entity.SkyeMetric;
import com.dafy.skye.storm.druid.DruidClientDelegate;
import com.dafy.skye.topology.SkyeKafkaTopologyFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.MapperFeature;
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
import io.druid.jackson.DefaultObjectMapper;
import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.generated.StormTopology;
import org.apache.storm.kafka.spout.KafkaSpout;
import org.apache.storm.kafka.spout.KafkaSpoutConfig;
import org.apache.storm.kafka.spout.RecordTranslator;
import org.joda.time.DateTime;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import zipkin.Span;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Created by quanchengyun on 2017/8/24.
 */
@Configuration
@EnableConfigurationProperties({KafkaConfigProperties.class})
public class StormConfig {

    @Bean
    public RecordTranslator<String,List<Span>> recordTranslator(){
        return new SkyeRecordTranslator<String,List<Span>>();
    }

    @Bean
    public ObjectMapper objectMapper(){
        ObjectMapper mapper = new DefaultObjectMapper();
        mapper.configure(MapperFeature.AUTO_DETECT_GETTERS, true);
        mapper.configure(MapperFeature.AUTO_DETECT_FIELDS, true);
        mapper.configure(MapperFeature.AUTO_DETECT_IS_GETTERS, true);
        mapper.configure(MapperFeature.AUTO_DETECT_SETTERS, true);
        return mapper;
    }
    @Bean
    public SpanCodecComponent<List<Span>> spanCodecComponent(){
        return new SpanCodecComponent<List<Span>>();
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
                    e.printStackTrace();
                }
            }
        };
    }


    @Bean(destroyMethod="destroy")
    public DruidClientDelegate initDruidClientDelegate(Timestamper<SkyeMetric> timestamper,ObjectWriter<SkyeMetric> objectWriter,Partitioner<SkyeMetric> partitioner){
        DruidClientDelegate client = new DruidClientDelegate();
        InputStream ins = Thread.currentThread().getContextClassLoader().getResourceAsStream("tranquility.yml");
        TranquilityConfig<PropertiesBasedConfig> config = TranquilityConfig.read(ins);
        List<String> dataSources = config.getDataSources();
        for(String dataSource: dataSources){
            Tranquilizer<SkyeMetric> sender = DruidBeams.fromConfig(config.getDataSource(dataSource),timestamper,objectWriter).partitioner(partitioner)
                    .buildTranquilizer(config.getDataSource(dataSource).tranquilizerBuilder());
            sender.start();
            client.getTranquilizerMap().put(dataSource,sender);
        }
        return client;
    }

    @Bean
    public KafkaSpout skyeKafkaSpout(KafkaConfigProperties kafkaConfigProperties, SpanCodecComponent<List<Span>> spanCodecComponent
    , RecordTranslator<String,List<Span>> recordTranslator){
        KafkaSpoutConfig config = KafkaSpoutConfig.builder(kafkaConfigProperties.getBootstrapServers(), kafkaConfigProperties.getTopics())
                .setValue(spanCodecComponent).setMaxPollRecords(kafkaConfigProperties.getMaxPollSize()).setPollTimeoutMs(30000)
                .setRecordTranslator(recordTranslator)
                .setGroupId(kafkaConfigProperties.getConsumerGroup()).build();
        return new KafkaSpout<String,Span>(config);
    }

    @Bean
    public StormTopology initializeStorm(KafkaSpout skyeKafkaSpout){
        StormTopology tp = SkyeKafkaTopologyFactory.build0(skyeKafkaSpout);
        //Configuration
        Config conf = new Config();
        //conf.put("wordsFile", args[0]);
        conf.setDebug(true);
        //Topology run
        conf.put(Config.TOPOLOGY_MAX_SPOUT_PENDING, 1);

        LocalCluster cluster = new LocalCluster();
        cluster.submitTopology("Getting-Started-Toplogie", conf,tp);
        return null;
    }

}
