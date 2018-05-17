package com.dafy.skye.zipkin.extend.job;

import com.dafy.skye.elasticsearch.template.IndexTemplate;
import com.dafy.skye.zipkin.extend.dto.BasicQueryRequest;
import com.dafy.skye.zipkin.extend.query.ZipkinElasticsearchQuery;
import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.simple.SimpleJob;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import zipkin2.elasticsearch.internal.IndexNameFormatter;

import java.util.Calendar;
import java.util.List;

/**
 * Created by quanchengyun on 2018/5/8.
 */
public class SpanQpsCountJob implements SimpleJob {

    private static Logger log = LoggerFactory.getLogger(SpanQpsCountJob.class);

    @Autowired
    private RestHighLevelClient restClient;


    @Value("${qps.zipkin-prefix}")
    private List<String> zipkinIndexPrefix;

    @Value("${qps.prefix}")
    private List<String> indexPrefix;

    @Value("${qps.interval:5}")
    private int indexInterval;

    private IndexNameFormatter indexNameFormatter;

    private IndexTemplate indexTemplate;

    public void setIndexTemplate(IndexTemplate indexTemplate) {
        this.indexTemplate = indexTemplate;
    }

    public void setRestClient(RestHighLevelClient restClient) {
        this.restClient = restClient;
    }

    public void setIndexNameFormatter(IndexNameFormatter indexNameFormatter) {
        this.indexNameFormatter = indexNameFormatter;
    }

    /**
     * 取到当前5秒的qps统计，因为es的索引刷新时间设置为5s
     * 本任务1min统计计算一次。对于可能存在的数据延迟问题，1.如何检测到数据存在延迟？
     * 消费延迟的问题不应该在实时计算系统中出现，否则实时计算本身就不成立了
     * 保证不存在数据延迟【相对于刷新周期之前，也就是5秒之前的数据已全部到达】, then every thing is easy
     * 对于1min内尚未预计算的数据，那么只能交由es实时计算。 【客户端处理起来麻烦，处理2套逻辑】
     * 如果 5s计算一次，那么全部查询计算之后的数据即可
     * @param shardingContext
     */
    @Override
    public void execute(ShardingContext shardingContext) {
        //按天创建qps索引
        //数据来源为 来自 zipkin-span 时间序列统计，获取当天的索引，然后获取里面最大的文档id[按照timestamp 排序获取最新的文档，然后获取这个文档的_id值]
        //然后以此 id+1 ,从zipkin-span 索引聚合按照秒数粒度的时间序列，然后依次写入当天索引
        long now = System.currentTimeMillis();
        String index = indexNameFormatter.formatTypeAndTimestamp(null,now);
        try{
            //转换到自然秒数
            long start ,end= (now/1000)*1000;
            if(!checkIndexExists(index)){
                //创建索引
                if(!indexTemplate.ensureTemplate("qps_template")){
                    log.error("索引模板未设置");
                    return;
                }
                restClient.indices().create(new CreateIndexRequest(index));
                Calendar cl = Calendar.getInstance();
                cl.setTimeInMillis(end);
                cl.set(Calendar.HOUR_OF_DAY,0);
                cl.set(Calendar.MINUTE,0);
                cl.set(Calendar.SECOND,0);
                start = cl.getTimeInMillis();
            }else{
                String lastId = getLatestId(index);
                start =Long.valueOf(lastId)+1000;
            }
            end = end-5000;
            if(start>=end){
                log.info("skip to next period.");
                return;
            }
            //

        }catch(Exception e){
            log.error("qps 任务异常：",e);
        }

    }


    private boolean checkIndexExists(String index){
        try{
            Response response=  restClient.getLowLevelClient().performRequest("HEAD",index);
            if(response.getStatusLine().getStatusCode()==200){
                return true;
            }
        }catch(Exception e){
            log.error("检查索引存在异常：",e);
        }
        return false;
    }


    public List<?> getList(long start,long end){
        BasicQueryRequest request = new BasicQueryRequest();
        request.setEndTs(end);
        request.setLookback(end-start);
        SearchSourceBuilder builder = ZipkinElasticsearchQuery.searchSourceBuilder(request);
        //builder.
        return null;
    }



    //--------------------------------------------------------

    @Deprecated
    private String  getLatestId(String index){
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices(index);
        SearchSourceBuilder searchSourceBuilder= new SearchSourceBuilder();
        QueryBuilder queryBuilder =  QueryBuilders.matchAllQuery();

        searchSourceBuilder.size(1);
        searchSourceBuilder.query(queryBuilder).sort("_timestamp", SortOrder.DESC);
        searchRequest.source(searchSourceBuilder);
        try{
            SearchResponse response = restClient.search(searchRequest);
            System.out.println(response);
        }catch(Exception e){
            log.error("SpanQpsCountJob exception: ",e);
        }
        return null;
    }
}
