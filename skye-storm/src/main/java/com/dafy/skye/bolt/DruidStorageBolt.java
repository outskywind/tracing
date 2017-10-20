package com.dafy.skye.bolt;

import com.dafy.skye.entity.SkyeMetric;
import com.dafy.skye.storm.druid.DruidCallback;
import com.dafy.skye.storm.druid.DruidClientDelegate;
import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.tuple.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 输出到druid集群供查询
 * Created by quanchengyun on 2017/8/25.
 */
public class DruidStorageBolt extends BaseBasicBolt{

    private static Logger log = LoggerFactory.getLogger(DruidStorageBolt.class);

    private static String DEFAULT_TABLE="span-metric";

    private static String TRACE_TABLE="trace-metric";

    /**
     * 最后一个bolt
     * @param input
     * @param collector
     */
    @Override
    public void execute(Tuple input, BasicOutputCollector collector) {
        //存储到druid
        SkyeMetric metric = (SkyeMetric) input.getValue(0);
        String table = DEFAULT_TABLE;
        if (metric.isTraceComplete()){
            table = TRACE_TABLE;
        }
        //此回掉函数那在druid client 线程执行的
        //而storm的bolt线程已经执行完成了，下一轮去了
        DruidClientDelegate.sendAsync(metric, table, new DruidCallback() {
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

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        //没有输出
    }

}
