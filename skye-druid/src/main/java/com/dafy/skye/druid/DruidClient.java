package com.dafy.skye.druid;

import com.dafy.skye.component.BalanceStrategy;
import com.dafy.skye.component.GranularitySimple;
import com.dafy.skye.component.ServerInfo;
import com.dafy.skye.component.Strategy;
import com.dafy.skye.druid.entity.QueryParam;
import com.dafy.skye.zk.ZkSerivceDiscovery;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import com.metamx.common.lifecycle.Lifecycle;
import com.metamx.http.client.HttpClient;
import io.druid.client.DirectDruidClient;
import io.druid.java.util.common.granularity.PeriodGranularity;
import io.druid.java.util.common.guava.Sequence;
import io.druid.java.util.common.guava.Sequences;
import io.druid.query.QueryMetrics;
import io.druid.query.QueryPlus;
import io.druid.query.Result;
import io.druid.query.select.EventHolder;
import io.druid.query.select.SelectResultValue;
import io.druid.query.topn.DefaultTopNQueryMetrics;
import io.druid.query.topn.TopNQuery;
import io.druid.query.topn.TopNQueryBuilder;
import io.druid.query.topn.TopNResultValue;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import java.io.Closeable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by quanchengyun on 2017/9/15.
 */
public class DruidClient<T> implements  Closeable{

    private static Logger LOG= LoggerFactory.getLogger(DruidClient.class);

    @Autowired
    protected DruidConfigurationProperties config;

    @Autowired
    protected ZkSerivceDiscovery zkSerivceDiscovery;

    protected Map<String,DirectDruidClient> cached_clients ;

    private Lifecycle lifecycle;
    private HttpClient httpClient;
    private ObjectMapper jsonMapper;

    public DruidClient(Map<String,DirectDruidClient> directDruidClient,HttpClient httpClient,Lifecycle lifecycle){
        this.cached_clients=directDruidClient;
        this.lifecycle = lifecycle;
        this.httpClient = httpClient;
    }


    //-----public  user interface -----------------

    public QueryPlus<Result<TopNResultValue>> buildTopNQuery(QueryParam param){
        DateTime start = new DateTime(param.getStartTimestamp()).toDateTime(DateTimeZone.UTC);
        StringBuilder sb = new StringBuilder(start.toString()) ;
        DateTime end = new DateTime(param.getEndTimestamp()).toDateTime(DateTimeZone.UTC);
        sb.append("/").append(end.toString());
        TopNQueryBuilder builder = new TopNQueryBuilder();
        builder.dataSource(param.getDataSource()).intervals(sb.toString())
                .granularity(new PeriodGranularity(Period.parse(param.getGranularity().value),null, DateTimeZone.getDefault()))
                .metric(param.getMetric()).threshold(config.getMaxTotalSeries());
        if(StringUtils.hasText(param.getGroupBy())){
            builder.dimension(param.getGroupBy());
        }
        TopNQuery query= builder.build();
        return  QueryPlus.wrap(query);
    }





    //------not public to users---------------------

    public Sequence<T> sendQuery(QueryPlus<T> query, String queryKey){
        DirectDruidClient sender = nextBalanceBroker(queryKey);
        final Map<String, Object> context = Maps.newHashMap();
        try {
            LOG.debug("Issuing query: %s", getJsonMapper().writeValueAsString(query));
        }
        catch (Exception e) {
            throw Throwables.propagate(e);
        }
        // Fetch the results.
        long startTime = System.currentTimeMillis();
        Sequence<Result<SelectResultValue>> resultSequence = sender.run(query, context);
        List<Result<SelectResultValue>> resultList = Sequences.toList(
                resultSequence,
                Lists.<Result<SelectResultValue>>newArrayList()
        );
        long fetchTime = System.currentTimeMillis() - startTime;
        LOG.debug("query cost time {}ms", fetchTime);
        // Print the results.
        int resultCount = 0;
        for (final Result<SelectResultValue> result : resultList) {
            for (EventHolder eventHolder : result.getValue().getEvents()) {
                LOG.debug("query event result {}",eventHolder.getEvent());
                resultCount++;
            }
        }
        return null;
    }

    public Sequence<T> sendQuery(QueryPlus<T> query){
        return sendQuery(query,null);
    }


    /**
     * get Brokers from zk cache each time for fresh
     * @return
     */
    protected DirectDruidClient nextBalanceBroker(){
        return nextBalanceBroker(null);
    }

    /**
     * get Brokers from zk cache each time for fresh
     * @return
     */
    protected DirectDruidClient nextBalanceBroker(String balanceKey){

        List<ServerInfo> serversInfo = getBrokersInfo();
        Strategy strategy = null;
        if (StringUtils.hasText(balanceKey)){
            strategy = BalanceStrategy.CONST_HASH.strategy;
        }
        else {
            String strategyName = config.getBanlanceStrategy();
            strategy = BalanceStrategy.valueOf(strategyName).strategy;
        }
        if(strategy==null){
            strategy = BalanceStrategy.ROUND_ROBIN.strategy;
        }
        //
        ServerInfo choosedServer = strategy.chooseNext(serversInfo);
        //first we check the cached client if exists previously
        //if not ,we decide it's new to the server cluster
        //then we need to build a new client connection
        if(!cached_clients.containsKey(choosedServer.getHost())){
            DruidClientBuilder.addNewHost(this, Arrays.asList(choosedServer.getHost()));
        }
        // since this host was selected from zk as live broker
        //so we don't need to worry about the cached dropped brokers
        return cached_clients.get(choosedServer.getHost());
    }


    public List<ServerInfo> getBrokersInfo(){
        String[] brokers = zkSerivceDiscovery.getBrokers();
        return null;
    }


    public DirectDruidClient<T> buildClient(){
        return null;
    }

    @Override
    public void close(){
        lifecycle.stop();
    }


    public Lifecycle getLifecycle() {
        return lifecycle;
    }

    public void setLifecycle(Lifecycle lifecycle) {
        this.lifecycle = lifecycle;
    }

    public HttpClient getHttpClient() {
        return httpClient;
    }

    public void setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public ObjectMapper getJsonMapper() {
        return jsonMapper;
    }

    public void setJsonMapper(ObjectMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }
}
