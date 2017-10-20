package com.dafy.skye.topology;

import com.dafy.skye.bolt.AggregateSpansBolt;
import com.dafy.skye.bolt.DruidStorageBolt;
import com.dafy.skye.bolt.SkyeKafkaBolt;
import org.apache.storm.generated.StormTopology;
import org.apache.storm.kafka.spout.KafkaSpout;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.tuple.Fields;

/**
 * Created by quanchengyun on 2017/8/24.
 */
public class SkyeKafkaTopologyFactory {

    public static StormTopology build0(KafkaSpout skyeKafkaSpout){
        final TopologyBuilder tp = new TopologyBuilder();
        //接收一批字数组，因此需要拆分一个个message处理
        tp.setSpout("kafka_spout",skyeKafkaSpout , 1);
        //拆分一个message中的spans列表
        tp.setBolt("zipkin_span_plit",new SkyeKafkaBolt()).shuffleGrouping("kafka_spout");
        //然后根据traceId分组分派任务流节点
        tp.setBolt("aggregate", new AggregateSpansBolt()).fieldsGrouping("zipkin_span_plit",new Fields("traceId"));
        tp.setBolt("storage", new DruidStorageBolt()).shuffleGrouping("aggregate");
        return tp.createTopology();
    }




}



