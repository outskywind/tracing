package com.dafy.skye.zipkin.extend;

import com.dafy.skye.druid.rest.*;
import com.dafy.skye.zipkin.extend.dto.ServiceSeriesRequest;
import com.dafy.skye.zipkin.extend.util.TimeUtil;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Created by quanchengyun on 2018/5/16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = SkyeMonitorApplication.class,webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties={"env=dev"})
public class ServiceTest {

    @Autowired
    RestDruidClient restDruidClient;

    @Test
    public void testDruid(){

        ServiceSeriesRequest request = new ServiceSeriesRequest();
        request.setEnd(System.currentTimeMillis());
        request.setStart(request.getEnd()-3600000);
        request.setTimeInterval("1m");
        request.setService("paymentcenter");

        GroupByQueryBuilder builder = GroupByQueryBuilder.builder();
        builder.dataSource("service-span-metric").dimensions(new String[]{"spanName"})
                .filter(Filter.builder().type(LogicType.selector).dimension("serviceName").value(request.getService()))
                .granularity(TimeUtil.parseTimeInterval(request.getTimeInterval()),new DateTime(request.getStart()))
                .timeRange(TimeInterval.builder().start(new DateTime(request.getStart())).end(new DateTime(request.getEnd())));
        restDruidClient.groupby(builder);
    }


}
