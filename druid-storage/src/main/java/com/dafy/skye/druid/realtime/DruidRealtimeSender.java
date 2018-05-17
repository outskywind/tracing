package com.dafy.skye.druid.realtime;

import com.dafy.skye.druid.entity.SkyeMetric;
import com.metamx.tranquility.tranquilizer.MessageDroppedException;
import com.metamx.tranquility.tranquilizer.Tranquilizer;
import com.twitter.util.FutureEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.runtime.BoxedUnit;

import javax.annotation.PreDestroy;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by quanchengyun on 2018/5/14.
 */
public class DruidRealtimeSender {

    private Logger log = LoggerFactory.getLogger(DruidRealtimeSender.class);

    private Map<String,Tranquilizer<SkyeMetric>> tranquilizerMap =new HashMap<>();

    public void add(String datasource,Tranquilizer<SkyeMetric> tranquilizer){
        this.tranquilizerMap.put(datasource,tranquilizer);
    }


    /**
     * 异步发送
     * @param obj
     * @param table
     * @param callback
     */
    public void sendAsync(final Object obj , String table , final DruidCallback callback){
        Tranquilizer sender = tranquilizerMap.get(table);
        if(sender==null){
            throw new IllegalStateException("sender "+table+" not initialized");
        }
        sender.send(obj).addEventListener(
                new FutureEventListener<BoxedUnit>()
                {
                    @Override
                    public void onSuccess(BoxedUnit value)
                    {
                        log.info("Sent message: {}", obj);
                        callback.success();
                    }

                    @Override
                    public void onFailure(Throwable e)
                    {
                        if (e instanceof MessageDroppedException) {
                            log.warn( "Dropped message: {}", obj,e);
                        } else {
                            log.error("Failed to send message: {}", obj,e);
                        }
                        callback.failed();
                    }
                }
        );
    }

    @PreDestroy
    public void destroy(){
        for(String key: this.tranquilizerMap.keySet()){
            this.tranquilizerMap.get(key).flush();
            this.tranquilizerMap.get(key).stop();
        }
    }

}
