package com.dafy.skye.log.server.controller;

import com.dafy.skye.common.util.TimeUtil;
import com.dafy.skye.log.server.storage.StorageComponent;
import com.dafy.skye.log.server.storage.query.CountMetric;
import com.dafy.skye.log.server.storage.query.LogQueryResult;
import com.dafy.skye.log.server.storage.query.LogSearchRequest;
import com.dafy.skye.log.server.storage.query.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


/**
 * Created by Caedmon on 2017/6/5.
 */
@RestController
@RequestMapping("/log")
public class LogQueryController {
    @Autowired
    private StorageComponent storageComponent;

    @RequestMapping("/query")
    Response search(@RequestBody LogSearchRequest request){
        LogQueryResult result=storageComponent.query(request);
        return new Response("0",result);
    }

    @RequestMapping("/series/count")
    Response countSeries(@RequestBody LogSearchRequest request){

        long timeInterval = TimeUtil.parseTimeIntervalSeconds(request.getTimeInterval());
        if(request.getEnd()==null){
            request.setEnd(System.currentTimeMillis());
        }
        long timeRange = (request.getEnd()- request.getStart())/1000;
        //
        if(timeRange/timeInterval>50){
            request.setTimeInterval(TimeUtil.adaptTimeInterval(timeRange,50));
        }
        List<CountMetric> result=storageComponent.countSeries(request);
        return new Response("0",result);
    }

}
