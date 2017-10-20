package com.dafy.skye.druid;

import com.dafy.skye.component.GranularitySimple;
import com.dafy.skye.druid.entity.QueryParam;
import com.dafy.skye.zk.ZkSerivceDiscovery;
import io.druid.java.util.common.granularity.Granularity;
import io.druid.java.util.common.granularity.PeriodGranularity;
import io.druid.query.Druids;
import io.druid.query.topn.TopNQueryBuilder;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * Created by quanchengyun on 2017/9/20.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class TestDruidClient {
    @Autowired
    ZkSerivceDiscovery zkSerivceDiscovery;

    @Autowired
    DruidClient client;


    @Test
    public void testZK(){
        String[] brokers = zkSerivceDiscovery.getBrokers();
        for(String b:brokers){
            System.out.print(b);
        }
    }

    @Test
    public void testQueryDruid(){
        Calendar cl = Calendar.getInstance();
        long end = cl.getTimeInMillis();
        long start = end-36000000*24;

        QueryParam param = new QueryParam();
        param.setEndTimestamp(end);
        param.setStartTimestamp(start);
        param.setDataSource("span-metric");
        param.setMetric("count");
        param.setGroupBy("serviceName");
        param.setGranularity(GranularitySimple.$1MIN);
        client.sendQuery(client.buildTopNQuery(param));

    }





}
