package com.dafy.skye.zipkin.extend.service;

import com.dafy.skye.zipkin.extend.dto.MonitorMetric;
import com.dafy.skye.zipkin.extend.dto.prometheus.QueryResult;
import com.dafy.skye.zipkin.extend.dto.prometheus.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Created by quanchengyun on 2018/10/21.
 */

@Service
public class PrometheusService {

    Logger log = LoggerFactory.getLogger(PrometheusService.class);
    //public static String QUERY_URL = "http://{host}/api/v1/query_range?";

    public static String QUERY_URL_PARAM = "http://{host}/api/v1/query?query={query}&time={end}";

    // http://prometheus.7daichina.com/api/v1/query_range?query=sum(request_latency_in_seconds_count%7Bexported_service%3D%22paymentcenter%22%7D)%20by%20(interfaceName)&start=1540180466.457&end=1540187666.457&step=28&_=1540121302229
    //其实是毫秒不是秒
    public String requestCount =   "sum(sum_over_time(request_latency_in_seconds_count{exported_service=\"{service}\"}[{duration}])) by (interfaceName)";

    public String requestSuccessCount = "sum(sum_over_time(request_latency_in_seconds_count{exported_service=\"{service}\",status=\"1\"}[{duration}])) by (interfaceName)";

    public String requestLatnecy =  "sum(sum_over_time(request_latency_in_seconds_sum{exported_service=\"{service}\"}[{duration}])) by (interfaceName)";

    @Autowired
    AsyncHttpClient httpClient;

    @Value("${prometheus.server}")
    public void setServer(String server){
        //QUERY_URL = QUERY_URL.replace("{host}",server);
        QUERY_URL_PARAM = QUERY_URL_PARAM.replace("{host}",server);
    }

    /**
     * 有巨坑
     * @param step
     */
    /*@Value("${prometheus.step}")
    public void setStep(String step){
        QUERY_URL = QUERY_URL.replace("{step}",step);
        QUERY_URL_PARAM = QUERY_URL_PARAM.replace("{step}",step);
    }*/

    @Autowired
    ObjectMapper json ;
    /**
     * 返回指定服务下接口指标
     * @param service
     * @param start
     * @param end
     * @return
     */
    public List<MonitorMetric> getInterfaceProfileMetric(String service, long start , long end){
            //
        List<MonitorMetric>  metrics = new ArrayList<>();
        //获取接口总次数
        Map<String,Long> requestCount = getRequestCount(service,start,end);
        //成功的次数
        Map<String,Long> requestSuccessCount = getRequestSuccessCount(service,start,end);
        //延迟总和
        Map<String,Long>  requestLatency = getRequestLatency(service,start,end);

        //Map<String,Long> peak_Qps = getMaxCount();  峰值qps没有了，需要新的指标

        for(String name : requestCount.keySet()){
            MonitorMetric metric = new MonitorMetric();
            //
            metric.setName(name);
            long count = requestCount.get(name);
            if(count==0){
                continue;
            }
            long success = requestSuccessCount.get(name)==null?0:requestSuccessCount.get(name);
            metric.setCount(count);
            double sc = div(success,count,1)*100;
            metric.setSuccessPercent(sc);
            metric.setSuccess_rate(sc+"%");
            //总延迟/次数
            long latency = requestLatency.get(name);
            metric.setLatency(latency/count);
            //qps
            metric.setQps(div(count,(end-start)/1000,1));
            //
            metrics.add(metric);
        }
        return metrics;
    }


    private static double div(long div1, long div2, int scale){
        BigDecimal bigDecimal = new BigDecimal(div1);
        BigDecimal bigDecimal2 = new BigDecimal(div2);
        return bigDecimal.divide(bigDecimal2, scale, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    /**
     * 小心科学计数法表示
     * @param div1
     * @param div2
     * @param scale
     * @return
     */
    private static String divToString(long div1, long div2, int scale){
        BigDecimal bigDecimal = new BigDecimal(div1);
        BigDecimal bigDecimal2 = new BigDecimal(div2);
        return bigDecimal.divide(bigDecimal2, scale, BigDecimal.ROUND_HALF_UP).toPlainString();
    }

    private Map<String,Long> getRequestLatency(String service, long start, long end) {
        Map<String,Long> result = new HashMap<>();
        try{
            QueryResult queryResult =buildQueryAndRequest(requestLatnecy,service,start,end);
            if(queryResult.getData()!=null){
                for(Result r : queryResult.getData().getResult()){
                    List<Object> value = r.getValue();
                    String v = (String)(value.get(1));
                    result.put(r.getMetric().get("interfaceName"),Long.valueOf(v));
                }
            }
        }catch (Exception e){
            log.error("prometheus query failed,service={}",service,e);
        }
        return result;
    }

    private Map<String,Long> getRequestSuccessCount(String service, long start, long end) {
        Map<String,Long> result = new HashMap<>();
        try{
            QueryResult queryResult =buildQueryAndRequest(requestSuccessCount,service,start,end);
            if(queryResult.getData()!=null){
                for(Result r : queryResult.getData().getResult()){
                    List<Object> value = r.getValue();
                    String v = (String)(value.get(1));
                    result.put(r.getMetric().get("interfaceName"),Long.valueOf(v));
                }
            }
        }catch (Exception e){
            log.error("prometheus query failed,service={}",service,e);
        }
        return result;
    }
    /**
     * //获取接口次数
     * @return
     */
    private Map<String,Long>   getRequestCount(String service, long start , long end){
         Map<String,Long> result = new HashMap<>();
         try{
             QueryResult queryResult =buildQueryAndRequest(requestCount,service,start,end);
             if(queryResult.getData()!=null){
                 for(Result r : queryResult.getData().getResult()){
                     List<Object> value = r.getValue();
                     String v = (String)(value.get(1));
                     result.put(r.getMetric().get("interfaceName"),Long.valueOf(v));
                 }
             }
         }catch (Exception e){
             log.error("prometheus query failed,service={}",service,e);
         }
         return result;
     }

    QueryResult buildQueryAndRequest(String prome_query, String service, long start, long end){
        QueryResult queryResult = null;
        try{
            //Double.toString 会用科学计数法表示
            String durationStr = new StringBuilder().append((int)Math.ceil(div((end-start)/1000,60,1))).append("m").toString();
            //String endStr = new StringBuilder().append(divToString(end,1000,3)).toString();
            String promeQuery = prome_query.replace("{service}",service).replace("{duration}",durationStr);
            String httpQuery  = QUERY_URL_PARAM.replace("{query}",escapeURIPathParam(promeQuery)).replace("{end}",divToString(end,1000,3));
            String jsonStr = do_http_query(httpQuery);
            queryResult =json.readValue(jsonStr,QueryResult.class);
        }catch (Exception e){
            log.error("prometheus query failed,service={}",service,e);
        }
        return queryResult;
    }

    protected String  do_http_query(String queryStr){
        Future<Response> responseFuture =   httpClient.preparePost(queryStr).execute();
        try{
            return responseFuture.get(5, TimeUnit.SECONDS).getResponseBody(Charset.forName("utf-8"));
        }catch (Exception e){
            log.error("query failed for {}",queryStr,e);
        }
        return null;
    }

    public static String escapeURIPathParam(String input) {
        StringBuilder resultStr = new StringBuilder();
        for (char ch : input.toCharArray()) {
            if (isUnsafe(ch)) {
                resultStr.append('%');
                resultStr.append(toHex(ch / 16));
                resultStr.append(toHex(ch % 16));
            } else{
                resultStr.append(ch);
            }
        }
        return resultStr.toString();
    }

    private static char toHex(int ch) {
        return (char) (ch < 10 ? '0' + ch : 'A' + ch - 10);
    }

    private static boolean isUnsafe(char ch) {
        if (ch > 128 || ch < 0)
            return true;
        return " %$&+,/:;=?@<>#%\"{}".indexOf(ch) >= 0;
    }




}
