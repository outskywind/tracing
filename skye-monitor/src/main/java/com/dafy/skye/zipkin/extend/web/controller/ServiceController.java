package com.dafy.skye.zipkin.extend.web.controller;

import com.dafy.skye.druid.entity.GroupbyQueryResult;
import com.dafy.skye.druid.entity.TimeSeriesQueryResult;
import com.dafy.skye.druid.rest.*;
import com.dafy.skye.zipkin.extend.cache.RulesRefreshHolder;
import com.dafy.skye.zipkin.extend.cache.ServiceRefreshHolder;
import com.dafy.skye.zipkin.extend.converter.DruidResultConverter;
import com.dafy.skye.zipkin.extend.dto.*;
import com.dafy.skye.zipkin.extend.enums.ResponseCode;
import com.dafy.skye.zipkin.extend.service.RuleService;
import com.dafy.skye.zipkin.extend.service.ZipkinExtendService;
import com.dafy.skye.zipkin.extend.util.TimeUtil;
import com.dafy.skye.zipkin.extend.service.UserService;
import org.joda.time.DateTime;
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
    RestDruidClient restDruidClient;

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
        timeRange = StringUtils.hasText(timeRange)?timeRange:"-10m";
        long now = System.currentTimeMillis();
        String str1=timeRange.substring(0,1);
        long unit = TimeUtil.parseTimeInterval(timeRange.substring(1));
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
        Rule[] rules = null;
        //因为是 服务面板数据，因此使用默认的或者服务粒度的
        for(ServiceMonitorMetric monitorMetric:result){
            rules = rulesRefreshHolder.getRules(monitorMetric.getName());
            ruleService.decideStat(monitorMetric,rules);
            List<MonitorMetric> servers = monitorMetric.getServers();
            for(MonitorMetric server: servers){
                ruleService.decideStat(server,rules);
            }
        }
        return new Response("0",result);
    }



    @RequestMapping("/series")
    @ResponseBody
    public Response  series(@RequestBody ServiceSeriesRequest request){
        //druid 一次最多5000个时序点因此如果时间序列250个点，只能一次返回最多20条线
        //时间粒度需要后端重新计算校验
        GroupByQueryBuilder builder = GroupByQueryBuilder.builder();
        builder.dataSource("service-span-metric").dimensions(new String[]{"spanName"})
                .filter(Filter.builder().type(LogicType.selector).dimension("serviceName").value(request.getService()))
                .granularity(TimeUtil.parseTimeInterval(request.getTimeInterval()),new DateTime(request.getStart()))
                .timeRange(TimeInterval.builder().start(new DateTime(request.getStart())).end(new DateTime(request.getEnd())))
                .addAggregation(Aggregation.builder().name("latency").type(AggregationType.longSum).fieldName("duration"))
                .addAggregation(Aggregation.builder().name("_count").type(AggregationType.longSum).fieldName("count"))
                .addPostAggregation(PostAggregation.builder().name("avg_latency").fn(Fn.div)
                        .fields(Arrays.asList(new PostAggregation.PostAggregationField("latency"),new PostAggregation.PostAggregationField("_count"))));
        List<GroupbyQueryResult> queryResult =  restDruidClient.groupby(builder);

        return  new Response("0",DruidResultConverter.convertSeriesMetricGroupby(queryResult,"spanName",request.getTimeInterval()));
    }


    @RequestMapping("/interface/profile")
    @ResponseBody
    public Response  interfaceProfile(@RequestBody ServiceSeriesRequest request){
        //druid 一次最多5000个时序点因此如果时间序列250个点，一次返回最多20条线
        //时间粒度需要后端重新计算校验
        GroupByQueryBuilder builder = GroupByQueryBuilder.builder();
        builder.dataSource("service-span-metric").dimensions(new String[]{"spanName"})
                .filter(Filter.builder().type(LogicType.selector).dimension("serviceName").value(request.getService()))
                .granularity(Granularity.all)
                .timeRange(TimeInterval.builder().start(new DateTime(request.getStart())).end(new DateTime(request.getEnd())))
                .addAggregation(Aggregation.builder().name("latency").type(AggregationType.longSum).fieldName("duration"))
                .addAggregation(Aggregation.builder().name("success").type(AggregationType.longSum).fieldName("successCount"))
                .addAggregation(Aggregation.builder().name("peak_qps").type(AggregationType.longMax).fieldName("count"))
                .addAggregation(Aggregation.builder().name("_count").type(AggregationType.longSum).fieldName("count"))
                .addPostAggregation(PostAggregation.builder().name("avg_latency").fn(Fn.div)
                        .fields(Arrays.asList(new PostAggregation.PostAggregationField("latency"),new PostAggregation.PostAggregationField("_count"))))
                .addPostAggregation(PostAggregation.builder().name("success_rate").fn(Fn.div)
                        .fields(Arrays.asList(new PostAggregation.PostAggregationField("success"),new PostAggregation.PostAggregationField("_count"))));
        List<GroupbyQueryResult> queryResult =  restDruidClient.groupby(builder);
        Collection<MonitorMetric> monitorMetrics = DruidResultConverter.convertMetric(queryResult,"spanName");
        //根据规则判断状态
        Rule[] rules ;
        for(MonitorMetric monitorMetric: monitorMetrics){
            rules = rulesRefreshHolder.getRules(monitorMetric.getName());
            ruleService.decideStat(monitorMetric,rules);
        }
        return  new Response("0",monitorMetrics);
    }


    @RequestMapping("/interface/series")
    @ResponseBody
    public Response interfaceSeries(@RequestBody InterfaceSeriesRequest request){

        TimeSeriesQueryBuilder builder = TimeSeriesQueryBuilder.builder();
        builder.dataSource("service-span-metric")
                .filter(Filter.builder().type(LogicType.and).fields(
                        Field.builder().type(Field.FieldType.selector).dimension("serviceName").value(request.getService()),
                        Field.builder().type(Field.FieldType.selector).dimension("spanName").value(request.getName())))
                .granularity(TimeUtil.parseTimeInterval(request.getTimeInterval()),new DateTime(request.getStart()))
                .timeRange(TimeInterval.builder().start(new DateTime(request.getStart())).end(new DateTime(request.getEnd())))
                .addAggregation(Aggregation.builder().name("latency").type(AggregationType.longSum).fieldName("duration"))
                .addAggregation(Aggregation.builder().name("_count").type(AggregationType.longSum).fieldName("count"))
                .addPostAggregation(PostAggregation.builder().name("avg_latency").fn(Fn.div)
                        .fields(Arrays.asList(new PostAggregation.PostAggregationField("latency"),new PostAggregation.PostAggregationField("_count"))));
        List<TimeSeriesQueryResult> queryResult =  restDruidClient.timeseries(builder);

        return  new Response("0",DruidResultConverter.convertSeriesMetricTimeseries(queryResult,request.getTimeInterval()));
    }








}
