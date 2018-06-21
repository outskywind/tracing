package com.dafy.skye.druid.rest;

import com.dafy.skye.component.BalanceStrategy;
import com.dafy.skye.druid.entity.GroupbyQueryResult;
import com.dafy.skye.druid.entity.TimeSeriesQueryResult;
import com.dafy.skye.zk.ZkServiceDiscovery;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.Future;

/**
 * Created by quanchengyun on 2018/5/11.
 */
public class RestDruidClient {

    Logger log = LoggerFactory.getLogger(RestDruidClient.class);

    ZkServiceDiscovery hostDiscovery;

    AsyncHttpClient httpClient;

    BalanceStrategy strategy= BalanceStrategy.ROUND_ROBIN;

    ObjectMapper json ;

    public void setHostDiscovery(ZkServiceDiscovery hostDiscovery) {
        this.hostDiscovery = hostDiscovery;
    }

    public void setHttpClient(AsyncHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public void setJson(ObjectMapper json) {
        this.json = json;
    }


    /**
     *
     * @param builder
     * @return
     */
    public List<TimeSeriesQueryResult> timeseries(TimeSeriesQueryBuilder builder){
        try{
            String requestBody = builder.build();
            Response r = sendQuery(requestBody).get();
            String responseBody = r.getResponseBody(Charset.forName("UTF-8"));
            return json.readValue(responseBody,new TypeReference<List<TimeSeriesQueryResult>>(){});
        }catch(Exception e){
            log.error("exception : ",e);
        }
        return null;
    }

    /**
     *
     * @param builder
     * @return
     */
    public List<GroupbyQueryResult> groupby(GroupByQueryBuilder builder){
        try{
            String requestBody = builder.build();
            Response r = sendQuery(requestBody).get();
            String responseBody = r.getResponseBody(Charset.forName("UTF-8"));
            return json.readValue(responseBody,new TypeReference<List<GroupbyQueryResult>>(){});
        }catch(Exception e){
            log.error("exception : ",e);
        }
        return null;
    }

    //because we are running in the servlet, the servlet is synchronized mode
    // respectively servlet 3.1 has async mode?
    protected  Future<Response> sendQuery(String requestBody,String contentType){
        try{
            String[] brokers = hostDiscovery.getBrokers();
            //
            String broker = strategy.chooseNext(brokers);
            Future<Response> response = httpClient.preparePost("http://"+broker+"/druid/v2/?pretty")
                    .setBody(requestBody).setHeader("Content-Type",contentType).execute();
            return response;
        }catch(Exception e){
            log.error("exception : ",e);
        }
        return null;
    }

    protected  Future<Response> sendQuery(String requestBody){
        return sendQuery(requestBody,"application/json");
    }





}
