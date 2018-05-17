package com.dafy.skye.qps.consumer;

import com.dafy.kafka.MessageConsumer;
import com.dafy.skye.qps.Qps;
import com.dafy.skye.zipkin.Decoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 统计自然秒数内的qps,时间戳取消息体的时间戳字段
 */
public class QpsMessageConsumer implements MessageConsumer {

    BulkQpsIndexer indexer;

    public void setIndexer(BulkQpsIndexer indexer) {
        this.indexer = indexer;
    }

    private Logger log = LoggerFactory.getLogger(QpsMessageConsumer.class);
    @Override
    public boolean accept(List<byte[]> messages) {
        List<zipkin2.Span> span2s = new ArrayList<>();
        for(byte[] message: messages){
            if(message.length==0) continue;
            try{
                span2s.addAll(Decoder.decode(message));
            }catch(RuntimeException e){
                log.error("exception: ",e);
            }
        }
        doAccept(span2s);
        return false;
    }


    protected boolean doAccept(List<zipkin2.Span> spans){
        for(zipkin2.Span span: spans){
            //确定 索引 ， 确定 type ,确定 id
            Qps qps = convertToQps(span);
            indexer.add(qps.getTimestamp(),qps);
        }
        return true;
    }


    private Qps convertToQps(zipkin2.Span span){
        Qps qps = new Qps();
        qps.setName(span.name());
        qps.setQps(1);
        qps.setTimestamp(span.timestamp()/1000);
        qps.setLocalEndpoint(span.localEndpoint());
        return qps;
    }



}
