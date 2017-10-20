package com.dafy.skye.components;


import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.storm.kafka.spout.DefaultRecordTranslator;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;
import zipkin.Span;


import java.util.List;

/**
 * Created by quanchengyun on 2017/8/25.
 */
public class SkyeRecordTranslator<K,V> extends DefaultRecordTranslator<K, V> {
    /**
     * @param record
     * @return
     */
    @Override
    public List<Object> apply(ConsumerRecord<K, V> record) {
        List<Object> result =  new Values(record.topic(),
                record.partition(),
                record.offset(),
                record.key(),
                record.value());
        //如何更优雅的处理泛型？
        //是一个元素的[] 数组来的List<Span>
        //实际上是一批数据每一个的traceId 不一定相同
        if(record.value() instanceof List){
            result.add(((List<Span>)record.value()).get(0).traceId);
        }
        else if(record.value() instanceof Span){
            result.add(((Span)record.value()).traceId);
        }
        return result;
    }

    @Override
    public Fields getFieldsFor(String stream) {
        //it's all right here because the toList() method generate a new List .
         List<String> f = FIELDS.toList();
         f.add("traceId");
         return new Fields(f);
    }



}
