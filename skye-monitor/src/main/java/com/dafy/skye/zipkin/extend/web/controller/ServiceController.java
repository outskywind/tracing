package com.dafy.skye.zipkin.extend.web.controller;

import com.dafy.skye.zipkin.extend.cache.RulesRefreshHolder;
import com.dafy.skye.zipkin.extend.cache.ServiceRefreshHolder;
import com.dafy.skye.zipkin.extend.dto.*;
import com.dafy.skye.zipkin.extend.enums.Dimension;
import com.dafy.skye.zipkin.extend.enums.ResponseCode;
import com.dafy.skye.zipkin.extend.enums.Stat;
import com.dafy.skye.zipkin.extend.service.PrometheusService;
import com.dafy.skye.zipkin.extend.service.RuleService;
import com.dafy.skye.zipkin.extend.service.ZipkinExtendService;
import com.dafy.skye.zipkin.extend.util.TimeUtil;
import com.dafy.skye.zipkin.extend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
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
    private ServiceRefreshHolder holder;

    @Autowired
    private RulesRefreshHolder rulesRefreshHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private PrometheusService prometheusService;

    @Autowired
    private ZipkinExtendService zipkinExtendService;

    @Autowired
    @Qualifier("ruleService")
    private RuleService ruleService;


    @RequestMapping("/closeIndex")
    public void closeIndex(@RequestParam("index.prefix") String indexPrefix){

    }


    @RequestMapping("/hosts")
    @ResponseBody
    public Response getHosts(@RequestBody Map<String,String> service){
        Set<String> hosts =  holder.getServiceHosts(service.get("service"));
        return new Response("0",hosts);
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
        Set<String> result= holder.getServiceInterfaces(service);
        return new Response("0",result);
    }



    @RequestMapping("/follow")
    @ResponseBody
    public Response followServices(@RequestBody List<String> services) {
        UserInfo user = getUser();
        if(CollectionUtils.isEmpty(services)){
            return new Response("no data error",null);
        }
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
        if(CollectionUtils.isEmpty(userInfo.getFavServices())){
            return new Response("未关注服务",null);
        }
        BasicQueryRequest request = new BasicQueryRequest();
        request.setEndTs(end);
        request.setLookback(end-start);
        request.setServices(Arrays.asList(userInfo.getFavServices().toArray(new String[0])));
        List<ServiceMonitorMetric> result = zipkinExtendService.getServiceMonitorMetrics(request);
        Rule[] rules;
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

    @Deprecated
    @RequestMapping("/series")
    @ResponseBody
    public Response  series(@RequestBody ServiceSeriesRequest request){
        Collection<TimeSeriesResult>  result = zipkinExtendService.getServiceSeries(request);
        return  new Response("0",result);
    }


    /***
     *  汇总一个接口里的一段时间范围里的统计值：
     *  次数，成功率
     *  平均响应时间
     * @param request
     * @return
     */
    @RequestMapping("/interface/profile")
    @ResponseBody
    public Response  interfaceProfile(@RequestBody ServiceSeriesRequest request){
        //Collection<MonitorMetric> monitorMetrics = zipkinExtendService.getInterfacesMonitorMetric(request);
        if(request.getEnd()==0){
            request.setEnd(System.currentTimeMillis());
        }
        if(request.getStart()==0){
            request.setStart(request.getEnd()-3600000);
        }
        Collection<MonitorMetric> monitorMetrics = prometheusService.getInterfaceProfileMetric(request.getService(),request.getStart(),request.getEnd());
        //根据规则判断状态
        Rule[] rules ;
        for(MonitorMetric monitorMetric: monitorMetrics){
            rules = rulesRefreshHolder.getRules(request.getService(),monitorMetric.getName());
            ruleService.decideStat(monitorMetric,rules);
        }
        MonitorMetric[] result = sortMetric(monitorMetrics);
        return  new Response("0",result);
    }


    @Deprecated
    @RequestMapping("/interface/series")
    @ResponseBody
    public Response interfaceSeries(@RequestBody InterfaceSeriesRequest request){
        Collection<SeriesMetric> seriesMetrics = zipkinExtendService.getInterfaceSeries(request);
        return  new Response("0",seriesMetrics);
    }


    @Deprecated
    @RequestMapping("/interface/traces")
    @ResponseBody
    public Response interfaceTraces(@RequestBody InterfaceSeriesRequest request){
        BasicQueryRequest queryRequest = new BasicQueryRequest();
        queryRequest.setEndTs(request.getEnd());
        queryRequest.setLookback(request.getEnd()-request.getStart());
        queryRequest.setServices(Arrays.asList(request.getService()));
        queryRequest.setSpans(Arrays.asList(request.getName()));
        queryRequest.setLimit(1000);

        List<Trace> traces = zipkinExtendService.getInterfaceTraces(queryRequest);
        Rule[] rules  = rulesRefreshHolder.getRules(request.getService(),request.getName());
        for(Trace trace: traces){
            ruleService.decideStat(trace,rules);
        }
        //sort, failed first , then stats red  , then left
        // we need two pointers here pointing at failed tail , red tail ,then next
        // so the data is expandable , we need the link list
        sortTrace(traces);
        List<Trace> result = traces.size()>300?traces.subList(0,300):traces;
        return new Response("0",result);
    }


    private void sortTrace(List<Trace> traces){
        int tail= 0;
        LinkedList<Trace> failed  = new LinkedList();
        LinkedList<Trace> red  = new LinkedList();
        LinkedList<Trace> other  = new LinkedList();

        for(int i=0;i<traces.size();i++){
            Trace e = traces.get(i);
            if(!e.isSuccess()){
                failed.add(e);
                continue;
            }
            Map<String, Stat> stats =  e.getStat();
            if(Stat.red ==stats.get(Dimension.LATENCY.value()) || Stat.red ==stats.get(Dimension.SUCCESS_RATE.value()) ){
                red.add(e);
                continue;
            }
            other.add(e);
        }
        //merge
        for(int i=0;i<failed.size();i++,tail++){
            traces.set(tail,failed.get(i));
        }
        for(int i=0;i<red.size();i++,tail++){
            traces.set(tail,red.get(i));
        }
        for(int i=0;i<other.size();i++,tail++){
            traces.set(tail,other.get(i));
        }
    }

    //标红的排前面，并且调用量大的靠前
    private MonitorMetric[] sortMetric(Collection<MonitorMetric> metrics){
        MonitorMetric[] metric$L = metrics.toArray(new MonitorMetric[0]);
        insertionSortByCount(metric$L,false);
        //move red to head
        for(int i=1;i<metric$L.length;i++){
            //成功率first
            for(int j=i; j>0 && Stat.red ==metric$L[j].getStat().get(Dimension.SUCCESS_RATE.value())
                        && Stat.red !=metric$L[j-1].getStat().get(Dimension.SUCCESS_RATE.value());j--){
                MonitorMetric tmp = metric$L[j-1];
                metric$L[j-1] = metric$L[j];
                metric$L[j]  = tmp;
            }
            //then append 延迟red
            for(int j=i; j>0 && Stat.red ==metric$L[j].getStat().get(Dimension.LATENCY.value())
                         && Stat.red !=metric$L[j-1].getStat().get(Dimension.SUCCESS_RATE.value())
                            && Stat.red !=metric$L[j-1].getStat().get(Dimension.LATENCY.value());j--){
                MonitorMetric tmp = metric$L[j-1];
                metric$L[j-1] = metric$L[j];
                metric$L[j]  = tmp;
            }
        }
        return metric$L;
    }

    /**
     *
     * @param metrics
     * @param asc
     */
    private void insertionSortByCount(MonitorMetric[] metrics, boolean asc){
        if(asc){
            for(int i=0 ; i<metrics.length;i++){
                for(int j=i; j>0 && metrics[j].getCount()<metrics[j-1].getCount();j--){
                    MonitorMetric tmp = metrics[j-1];
                    metrics[j-1] = metrics[j];
                    metrics[j]  = tmp;
                }
            }
        } else{
            for(int i=0 ; i<metrics.length;i++){
                for(int j=i; j>0 && metrics[j].getCount()>metrics[j-1].getCount();j--){
                    MonitorMetric tmp = metrics[j-1];
                    metrics[j-1] = metrics[j];
                    metrics[j]  = tmp;
                }
            }
        }
    }

}
