package com.dafy.skye.log.server.controller;

import com.dafy.skye.log.server.storage.StorageComponent;
import com.dafy.skye.log.server.storage.query.LogSearchRequest;
import com.dafy.skye.log.server.storage.query.LogQueryResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

/**
 * Created by Caedmon on 2017/6/5.
 */
@RestController
@RequestMapping("/api/v1/log")
public class LogQueryController {
    @Autowired
    private StorageComponent storageComponent;
    @RequestMapping("/search")
    LogQueryResult search(@RequestBody LogSearchRequest request){
        LogQueryResult result=storageComponent.query(request);
        return result;
    }
    @RequestMapping("/services")
    Set<String> getServices(){
        return storageComponent.getServices();
    }
}
