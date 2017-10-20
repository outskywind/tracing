package com.dafy.skye.bolt;

/**
 * Created by quanchengyun on 2017/8/24.
 */

import com.dafy.skye.constans.StormConstants;
import com.dafy.skye.entity.SkyeMetric;
import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import zipkin.Annotation;
import zipkin.BinaryAnnotation;
import zipkin.Span;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 判断一次trace调用链结束后，决定本次trace是否成功，失败，异常。
 * 本次trace算一次trace调用。
 * 本次trace包含的个接口服务独立统计次数
 * 本bolt为最后的处理链
 */
public class AggregateSpansBolt extends BaseBasicBolt{

    @Override
    public void execute(Tuple tuple, BasicOutputCollector basicOutputCollector) {
        Span span = (Span)tuple.getValueByField(StormConstants.SPAN);
        //判断状态
        Iterator<Annotation> it =  span.annotations.iterator();
        String serviceName=null;
        while(it.hasNext()){
            Annotation ann = it.next();
            serviceName = ann.endpoint.serviceName;
        }

        Iterator<BinaryAnnotation> bit =  span.binaryAnnotations.iterator();
        int success = 0;
        while(bit.hasNext()){
            BinaryAnnotation ann = bit.next();
            if("result".equals(ann.key) || ("status".equals(ann.key) && "success".equals(ann.value))){
                success = 1;
            }
        }
        SkyeMetric metric = new SkyeMetric();
        metric.setDuration(span.duration/1000.00);
        metric.setTraceComplete(span.traceId == span.id);
        metric.setSpanName(span.name);
        metric.setServiceName(serviceName);
        metric.setTimestamp(TimeUnit.MICROSECONDS.toMillis(span.timestamp));
        metric.setSuccess(success);
        metric.setException(1-success);
        List<Object> tuples = new ArrayList<Object>();
        tuples.add(metric);
        basicOutputCollector.emit(tuples);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields(StormConstants.METRIC));

    }
}
