package com.dafy.skye.storm.druid;

import com.dafy.skye.entity.SkyeMetric;
import com.metamx.tranquility.tranquilizer.MessageDroppedException;
import com.metamx.tranquility.tranquilizer.Tranquilizer;
import com.twitter.util.FutureEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.runtime.BoxedUnit;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * 发送数据到Druid集群,因为Bolt要求必须是Serializable接口的
 * 所以没办法只能使用静态调用方法
 */
public class DruidClientDelegate {

    private  static Logger log = LoggerFactory.getLogger(DruidClientDelegate.class);

    private static Map<String,Tranquilizer<SkyeMetric>> tranquilizerMap =new HashMap<String,Tranquilizer<SkyeMetric>>();


    /**
     * 异步发送，那么无论成功失败这个tuple都会被storm的bolt ack确认，storm将处理下一轮tuple
     * @param obj
     * @param table
     * @param callback
     */
    public static void sendAsync(final Object obj , String table , final DruidCallback callback){
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


    public void destroy(){
        for(String key: this.getTranquilizerMap().keySet()){
            this.getTranquilizerMap().get(key).flush();
            this.getTranquilizerMap().get(key).stop();
        }
    }


    public Map<String, Tranquilizer<SkyeMetric>> getTranquilizerMap() {
        return tranquilizerMap;
    }

    public void setTranquilizerMap(Map<String, Tranquilizer<SkyeMetric>> tranquilizerMap) {
        this.tranquilizerMap = tranquilizerMap;
    }
}
