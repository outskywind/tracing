package com.dafy.skye.log.server.controller;

import com.dafy.skye.log.server.storage.StorageComponent;
import com.dafy.skye.log.server.storage.query.LogQueryRequest;
import com.dafy.skye.log.server.storage.query.LogQueryResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by Caedmon on 2017/6/5.
 */
@RestController
@RequestMapping("/log/api/v1")
public class SkyeLogQueryController {
    @Autowired
    private StorageComponent storageComponent;
    @RequestMapping("/query")
    LogQueryResult query(@RequestBody LogQueryRequest request){
        LogQueryResult result=storageComponent.query(request);
        return result;
    }
}
