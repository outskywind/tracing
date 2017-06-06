package com.dafy.skye.server.controller;

import com.dafy.skye.log.collector.storage.query.LogQueryRequest;
import com.dafy.skye.server.dto.SkyeLogDTO;
import com.dafy.skye.server.service.SkyeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Created by Caedmon on 2017/6/5.
 */
@RestController("/api/log")
public class SkyeLogController {
    @Autowired
    private SkyeService skyeService;
    @RequestMapping("/query")
    List<SkyeLogDTO> query(LogQueryRequest request){
        List<SkyeLogDTO> entities=skyeService.queryLogs(request);
        return entities;
    }
}
