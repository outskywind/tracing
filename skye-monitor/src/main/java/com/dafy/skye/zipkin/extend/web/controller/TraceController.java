package com.dafy.skye.zipkin.extend.web.controller;

import com.dafy.skye.zipkin.extend.dto.*;
import com.dafy.skye.zipkin.extend.service.ZipkinExtendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

/**
 * Created by Caedmon on 2017/6/21.
 */
@RestController
@RequestMapping(value="/api/v1/trace", method = RequestMethod.POST)
public class TraceController {

    ZipkinExtendService zipkinExtendService;

    @RequestMapping("/services")
    Set<String> getServiceNames(@RequestBody BasicQueryRequest request){
        return zipkinExtendService.getServices(request);
    }

    public ZipkinExtendService getZipkinExtendService() {
        return zipkinExtendService;
    }

    @Autowired
    public void setZipkinExtendService(ZipkinExtendService zipkinExtendService) {
        this.zipkinExtendService = zipkinExtendService;
    }
}
