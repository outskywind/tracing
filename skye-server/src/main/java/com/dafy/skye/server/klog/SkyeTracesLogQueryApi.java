package com.dafy.skye.server.klog;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by Caedmon on 2017/4/24.
 */
@Controller
@RequestMapping("/query/api/v1")
public class SkyeTracesLogQueryApi {
    @GetMapping("/traces_log")
    public @ResponseBody String findByTraceId(String traceId){

    }
}
