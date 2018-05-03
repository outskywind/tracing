package com.dafy.skye.zipkin.extend.web.controller;

import com.dafy.skye.zipkin.extend.cache.RulesRefreshHolder;
import com.dafy.skye.zipkin.extend.cache.ServiceRefreshHolder;
import com.dafy.skye.zipkin.extend.dto.*;
import com.dafy.skye.zipkin.extend.enums.ResponseCode;
import com.dafy.skye.zipkin.extend.service.RuleService;
import com.dafy.skye.zipkin.extend.service.ZipkinExtendService;
import com.dafy.skye.zipkin.extend.util.TimeUtil;
import com.dafy.skye.zipkin.extend.service.UserService;
import com.dafy.skye.zipkin.extend.web.session.UserSessionHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Created by quanchengyun on 2018/3/28.
 */
@Controller
@RequestMapping(value="/service",method = RequestMethod.POST)
public class ServiceController extends BaseSessionController{

    @Autowired
    ServiceRefreshHolder holder;

    @Autowired
    RulesRefreshHolder rulesRefreshHolder;

    @Autowired
    UserService userService;

    @Autowired
    ZipkinExtendService zipkinExtendService;

    @Autowired
    @Qualifier("ruleService")
    RuleService ruleService;


    @RequestMapping("/closeIndex")
    public void closeIndex(@RequestParam("index.prefix") String indexPrefix){

    }

    @RequestMapping("/list/all")
    @ResponseBody
    public Response listAllServices(@RequestBody Map<String,String> filter){
        String[] services =holder.getServicesName();
        List<String> result = new ArrayList<>();
        UserInfo user = getUser();
        Set<String> follows  = user.getFavServices();
        String _filter = filter.get("filter");
        for(String service:services){
            if(!follows.contains(service) && (!StringUtils.hasText(_filter) || service.contains(_filter))){
                result.add(service);
            }
        }
        return new Response("0",result);
    }

    @RequestMapping("/interface/list")
    @ResponseBody
    public Response listInterfaces(@RequestBody Map<String,String> param){
        String service = param.get("service");
        List<String> result= holder.getServiceInterfaces(service);
        return new Response("0",result);
    }



    @RequestMapping("/follow")
    @ResponseBody
    public Response followServices(@RequestBody List<String> services) {
        UserInfo user = getUser();
        Set<String> set = new HashSet<>(services);
        ResponseCode code = userService.followServices(user.getEmail(),set);
        return new Response(code.value(),null);
    }


    @RequestMapping("/unfollow")
    @ResponseBody
    public Response unfollowServices(@RequestBody List<String> services) {
        UserInfo user = getUser();
        Set<String> set = new HashSet<>(services);
        ResponseCode code = userService.unfollowServices(user.getEmail(),set);
        return new Response(code.value(),null);
    }


    @RequestMapping("/list/follow")
    @ResponseBody
    public Response listFollowServices() {
        UserInfo user = getUser();
        List<String> services= userService.listFollowServices(user.getEmail());
        return new Response("0",services);
    }

    @RequestMapping("/show/stats")
    @ResponseBody
    public Response showStats(@RequestBody(required=false) Map<String,String> param){
        String timeRange = param==null?null:param.get("timeRange");
        timeRange = StringUtils.hasText(timeRange)?timeRange:"-1m";
        long now = System.currentTimeMillis();
        String str1=timeRange.substring(0,1);
        long unit = TimeUtil.parseTimeUnit(timeRange.substring(1));
        long start=0,end=0;
        if(unit>0){
            switch (str1) {
                case "-":start=now-unit;end=now;break;
                case "+":start=now;end=now+unit;break;
            }
        }
        if(start==0 || end==0){
            return new Response("参数格式错误",null);
        }
        //
        UserInfo userInfo = getUser();
        BasicQueryRequest request = new BasicQueryRequest();
        request.setEndTs(end);
        request.setLookback(end-start);
        request.setServices(Arrays.asList(userInfo.getFavServices().toArray(new String[0])));
        List<ServiceMonitorMetric> result = zipkinExtendService.getServiceMonitorMetrics(request);
        List<Rule> rules = rulesRefreshHolder.getRules();
        for(ServiceMonitorMetric monitorMetric:result){
            ruleService.decideStat(monitorMetric,rules);
            List<MonitorMetric> servers = monitorMetric.getServers();
            for(MonitorMetric server: servers){
                ruleService.decideStat(monitorMetric,rules);
            }
        }
        return new Response("0",result);
    }





}
