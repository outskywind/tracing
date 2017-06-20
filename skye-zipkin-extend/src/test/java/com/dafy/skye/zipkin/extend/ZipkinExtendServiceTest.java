package com.dafy.skye.zipkin.extend;

import com.dafy.skye.common.util.JacksonConvert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.Set;

/**
 * Created by Caedmon on 2017/6/18.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = ZipkinExtendApplication.class,webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class ZipkinExtendServiceTest {

    @Autowired
    private ZipkinExtendService zipkinExtendService;
    @Test
    public void testGetServiceNames(){
        Set<String> serviceNames=zipkinExtendService.getServiceNames(endTs(),lookup());
        System.out.println(serviceNames);
    }
    @Test
    public void testTraceStats(){
        TraceQueryRequest request=TraceQueryRequest.newBuilder().lookback(lookup()).endTs(endTs()).build();
        TraceQueryResult result=zipkinExtendService.getTraceStats(request);
        System.out.println(JacksonConvert.toPrettyString(result));
    }
    static long lookup(){
        return 604800000*10;
    }
    static long endTs(){
        return System.currentTimeMillis();
    }
}
