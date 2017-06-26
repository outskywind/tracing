package com.dafy.skye.zipkin.extend;

import com.dafy.skye.common.util.IntervalTimeUnit;
import com.dafy.skye.common.util.JacksonConvert;
import com.dafy.skye.zipkin.extend.dto.*;
import com.dafy.skye.zipkin.extend.service.ZipkinExtendServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Base64;
import java.util.Set;

/**
 * Created by Caedmon on 2017/6/18.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = ZipkinExtendApplication.class,webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class ZipkinExtendServiceTest {

    @Autowired
    private ZipkinExtendServiceImpl zipkinExtendService;
    @Test
    public void testGetServiceNames(){
        ServiceNameQueryRequest request=new ServiceNameQueryRequest();
        request.endTs=endTs();
        request.lookback=lookback();
        Set<String> serviceNames=zipkinExtendService.getServices(request);
        System.out.println(serviceNames);
    }
    @Test
    public void testTraceStats(){
        TraceQueryRequest request=TraceQueryRequest.newBuilder()
                .lookback(lookback())
                .endTs(endTs()).build();
        TraceMetricsResult result=zipkinExtendService.getTracesMetrics(request);
        System.out.println(JacksonConvert.toPrettyString(result));
    }
    @Test
    public void testGetTraces(){
        TraceQueryRequest request=TraceQueryRequest.newBuilder()
                .lookback(lookback())
                .endTs(endTs()).build();
        TraceQueryResult result=zipkinExtendService.getTraces(request);
        System.out.println(JacksonConvert.toPrettyString(result));
    }
    @Test
    public void testGetSpanStats(){
        TraceQueryRequest request=TraceQueryRequest.newBuilder()
                .lookback(lookback())
                .endTs(endTs())
                .interval(5).intervalUnit(IntervalTimeUnit.DAY).build();
        SpanMetricsResult result=zipkinExtendService.getSpansMetrics(request);
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
}
