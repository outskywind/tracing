package com.dafy.skye.zipkin.extend;

import com.dafy.skye.common.util.IntervalTimeUnit;
import com.dafy.skye.common.util.JacksonConvert;
import com.dafy.skye.zipkin.extend.dto.*;
import com.dafy.skye.zipkin.extend.job.ESIndexCloseJob;
import com.dafy.skye.zipkin.extend.service.ZipkinExtendServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Caedmon on 2017/6/18.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = ZipkinExtendApplication.class,webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties={"env=dev"})
public class ZipkinExtendServiceTest {

    @Autowired
    private ZipkinExtendServiceImpl zipkinExtendService;

    @Autowired
    ESIndexCloseJob job;
    @Test
    public void testGetServiceNames(){
        BasicQueryRequest request=new BasicQueryRequest();
        request.endTs=endTs();
        request.lookback=lookback();
        Set<String> serviceNames=zipkinExtendService.getServices(request);
        System.out.println(serviceNames);
    }

    @Test
    public void testGetTraces(){
        BasicQueryRequest request=BasicQueryRequest.newBuilder()
                .lookback(lookback())
                .endTs(endTs()).build();
        TraceQueryResult result=zipkinExtendService.getTraces(request);
        System.out.println(JacksonConvert.toPrettyString(result));
    }
    @Test
    public void testGetSpanStats(){
        BasicQueryRequest request=BasicQueryRequest.newBuilder()
                .lookback(lookback())
                .endTs(endTs())
                .interval(5).intervalUnit(IntervalTimeUnit.DAY).build();
        //SpanMetricsResult result=zipkinExtendService.geMetrics(request);
        //System.out.println(JacksonConvert.toPrettyString(result));
    }

    @Test
    public void testSpanTimeSeries(){
        BasicQueryRequest request=BasicQueryRequest.newBuilder()
                .lookback(lookback())
                .endTs(1498486059308L).spans(Arrays.asList("gatewayprovider.start","officeprovider.startwork")).
                        build();
        ServiceTimeSeriesResult result=zipkinExtendService.getServiceTimeSeries(request);
        System.out.println(JacksonConvert.toPrettyString(result));
    }


    public static void main(String[] args) {
        String s=new String(Base64.getDecoder().decode("c3VjY2Vzcw=="));
        System.out.println(s);
    }
    static long lookback(){
        return 6048000000L;
    }
    static long endTs(){
        return System.currentTimeMillis();
    }

    @Test
    public void testD() throws  Exception{
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date d = sdf.parse("2017-07-10");
        Calendar cl = Calendar.getInstance();
        cl.setTime(d);
        System.out.println(cl.getTime());
    }

    @Test
    public void TestESIndexCloseJob(){
        job.execute(null);
    }
}
