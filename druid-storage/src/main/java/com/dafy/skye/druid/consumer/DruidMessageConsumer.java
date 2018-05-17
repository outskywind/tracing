package com.dafy.skye.druid.consumer;

import com.dafy.kafka.MessageConsumer;
import com.dafy.skye.druid.entity.SkyeMetric;
import com.dafy.skye.druid.realtime.DruidCallback;
import com.dafy.skye.druid.realtime.DruidRealtimeSender;
import com.dafy.skye.zipkin.Decoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by quanchengyun on 2018/5/14.
 */
public class DruidMessageConsumer implements MessageConsumer {

    private DruidRealtimeSender sender;

    public void setSender(DruidRealtimeSender sender) {
        this.sender = sender;
    }

    private Logger log = LoggerFactory.getLogger(DruidMessageConsumer.class);
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
        return true;
    }

    private void doAccept(List<zipkin2.Span> span2s) {
        for(zipkin2.Span span:span2s){
            //String server = zipkin2.Span.Kind.SERVER.name();
            if (zipkin2.Span.Kind.SERVER.equals(span.kind())){
                SkyeMetric metric = new SkyeMetric();
                metric.setDuration(span.durationAsLong()/1000);
                int success = "success".equals(span.tags().get("status"))?1:0;
                metric.setSuccess(success);
                metric.setHost(span.localEndpoint().ipv4());
                metric.setServiceName(span.localServiceName());
                metric.setSpanName(span.name());
                metric.setTimestamp(span.timestampAsLong()/1000);
                //metric.set
                sender.sendAsync(metric, "service-span-metric", new DruidCallback() {
                    @Override
                    public void success() {
                        log.info("----success store to druid---");
                    }
                    @Override
                    public void failed() {
                        log.warn("----failed store to druid---");
                    }
                });
            }
        }
    }
}
