package com.dafy.skye.server.controller;

import com.dafy.skye.log.collector.storage.query.LogQueryRequest;
import com.dafy.skye.log.collector.storage.query.LogQueryResult;
import com.dafy.skye.server.service.SkyeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Created by Caedmon on 2017/6/5.
 */
@RestController
@RequestMapping("/api/logs")
public class SkyeLogController {
    @Autowired
    private SkyeService skyeService;
    @RequestMapping("/query")
    LogQueryResult query(@RequestBody LogQueryRequest request){
        LogQueryResult result=skyeService.logsQuery(request);
        return result;
    }
}
