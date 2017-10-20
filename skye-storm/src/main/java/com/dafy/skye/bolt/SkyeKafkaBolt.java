package com.dafy.skye.bolt;


import com.dafy.skye.constans.StormConstants;
import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import zipkin.Span;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by quanchengyun on 2017/8/25.
 */
public class SkyeKafkaBolt extends BaseBasicBolt {

    //使用hash gourping 那么会根据field hash分发的相同的那个bolt集群的某一个节点
    @Override
    public void execute(Tuple input, BasicOutputCollector collector) {
        List<Span> spans = (List<Span>)input.getValue(4);
        for(Span span:spans){
            List<Object> tuples = new ArrayList<Object>();
            tuples.add(span.traceId);
            tuples.add(span);
            collector.emit(tuples);
        }

    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields(StormConstants.TRACE_ID,StormConstants.SPAN));
    }

    /**
     * KafkaSpout 处理之后的流为 "topic", "partition", "offset", "key", "value"
     * refer to {@link org.apache.storm.kafka.spout.DefaultRecordTranslator }
     * @param declarer
     */

}
