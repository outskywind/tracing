package com.dafy.skye.log.server.storage.elasticsearch;

import com.dafy.skye.common.elasticsearch.DefaultOptions;
import com.dafy.skye.common.util.IndexNameFormatter;
import com.dafy.skye.common.util.JacksonConvert;
import com.dafy.skye.common.util.ResourceUtil;
import com.dafy.skye.log.core.logback.SkyeLogEvent;
import com.dafy.skye.log.server.autoconfig.LogStorageESConfigProperties;
import com.dafy.skye.log.server.storage.StorageComponent;
import com.dafy.skye.log.server.storage.entity.SkyeLogEntity;
import com.dafy.skye.log.server.storage.query.CountMetric;
import com.dafy.skye.log.server.storage.query.LogSearchRequest;
import com.dafy.skye.log.server.storage.query.LogQueryResult;
import com.fasterxml.jackson.databind.JavaType;
import com.google.common.base.Strings;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.admin.indices.template.get.GetIndexTemplatesRequestBuilder;
import org.elasticsearch.action.admin.indices.template.get.GetIndexTemplatesResponse;
import org.elasticsearch.action.admin.indices.template.put.PutIndexTemplateRequestBuilder;
import org.elasticsearch.action.admin.indices.template.put.PutIndexTemplateResponse;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.metadata.IndexTemplateMetaData;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.indices.IndexClosedException;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.histogram.ParsedDateHistogram;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

/**
 * Created by Caedmon on 2017/4/26.
 */
public class ElasticSearchStorage implements StorageComponent {
    private LogStorageESConfigProperties esConfig;
    private IndexNameFormatter indexNameFormatter;
    private static final Logger log= LoggerFactory.getLogger(ElasticSearchStorage.class);
    private TransportClient transportClient;

    public ElasticSearchStorage(LogStorageESConfigProperties esConfig){
        this.esConfig=esConfig;
    }
    @PostConstruct
    public void init() throws UnknownHostException{
        Settings settings = Settings.builder()
                .put("cluster.name", esConfig.getClusterName()).build();
        this.transportClient= new PreBuiltTransportClient(settings);
        for(String host: esConfig.getTransportHosts()){
            String[] array=host.split(":");
            this.transportClient
                    .addTransportAddress(new TransportAddress(
                            InetAddress.getByName(array[0]), Integer.parseInt(array[1])));
        }
        IndexNameFormatter.Builder formatterBuilder=IndexNameFormatter.builder();
        formatterBuilder.dateSeparator('-');
        final String index=esConfig.getIndex();
        this.indexNameFormatter=formatterBuilder.index(index).build();
    }
    /**
     * 不存在则创建一个模板
     */
    boolean ensureTemplate(){
        final IndicesAdminClient adminClient=transportClient.admin().indices();
        GetIndexTemplatesRequestBuilder getTemplatesRequest=adminClient.prepareGetTemplates();
        GetIndexTemplatesResponse getIndexTemplatesResponse=getTemplatesRequest.execute().actionGet();
        List<IndexTemplateMetaData> templateMetaDatas=getIndexTemplatesResponse.getIndexTemplates();
        String templateName=esConfig.getIndex()+"_template";
        if(templateMetaDatas!=null||!templateMetaDatas.isEmpty()){
            for(IndexTemplateMetaData metaData:templateMetaDatas){
                if(metaData.getName().equals(templateName)){
                    return true;
                }
            }
        }
        PutIndexTemplateRequestBuilder requestBuilder=adminClient
                .preparePutTemplate(templateName);
        //Settings.Builder settings=Settings.builder().put("number_of_shards",esConfig.getIndexShards());
        //settings.put("number_of_replicas",1);
        //requestBuilder.setSettings(settings.build());
        requestBuilder.setTemplate(esConfig.getIndex()+"-*");
        String mappingSource=ResourceUtil.readString("elasticsearch-template.json");
        String settings = ResourceUtil.readString("skye-elasticsearch-settings.json");
        requestBuilder.addMapping(esConfig.getType(),mappingSource, XContentType.JSON);
        requestBuilder.setSettings(settings,XContentType.JSON);
        PutIndexTemplateResponse putIndexTemplateResponse=requestBuilder.execute().actionGet();
        return putIndexTemplateResponse.isAcknowledged();
    }
    @Override
    public void start() {
        log.info("ElasticSearch Storage started");
        ensureTemplate();
    }

    @Override
    public void stop() {
        this.transportClient.close();
    }

    @Override
    public void save(final SkyeLogEvent event) throws Exception{
        String index=indexNameFormatter.formatTypeAndTimestamp(null,event.getTimeStamp());
        IndexRequestBuilder indexRequestBuilder=transportClient.prepareIndex(index,esConfig.getType());
        indexRequestBuilder.setOpType(DocWriteRequest.OpType.CREATE);
        SkyeLogEntity entity=SkyeLogEntity.build(event);
        indexRequestBuilder.setVersionType(null);
        String source=JacksonConvert.toJsonString(entity);
        indexRequestBuilder.setSource(source,XContentType.JSON);
        IndexResponse response=indexRequestBuilder.execute().actionGet();
    }

    @Override
    public void batchSave(Collection<SkyeLogEvent> events) throws Exception{
        BulkRequestBuilder bulkRequestBuilder=transportClient.prepareBulk();
        for(SkyeLogEvent event:events){
            String index=indexNameFormatter.formatTypeAndTimestamp(null,event.getTimeStamp());
            IndexRequestBuilder indexRequestBuilder=transportClient.prepareIndex(index,esConfig.getType());
            indexRequestBuilder.setOpType(DocWriteRequest.OpType.INDEX);
            SkyeLogEntity entity=SkyeLogEntity.build(event);
            String source=JacksonConvert.toJsonString(entity);
            indexRequestBuilder.setSource(source,XContentType.JSON);
            bulkRequestBuilder.add(indexRequestBuilder);
        }
        BulkResponse responses=bulkRequestBuilder.execute().actionGet();
        //全部失败,将不会更新kafka的offset，表明很可能是es服务问题
        if(responses.hasFailures()){
            log.warn("Message save to es has failures");
            int indexCloseCount = 0;
            int failureCount = 0;
            for (int i = 0; i < responses.getItems().length; i++) {
                BulkItemResponse response = responses.getItems()[i];
                if(response.getFailure()!=null){
                    failureCount++;
                    Exception e = response.getFailure().getCause() ;
                    //针对特殊情况
                    if(e instanceof IndexClosedException){
                        indexCloseCount++;
                    }
                }
                if (!response.isFailed()) {
                    return;
                }
            }
            if(indexCloseCount==failureCount){
                return ;
            }
            throw new Exception("Message all failed save to es");
        }
    }

    /**
     * from to 限制了3000条
     * 查询真正关心的是前10页
     * @param request
     * @return
     */
    @Override
    public LogQueryResult query(LogSearchRequest request) {
        if(request.getEnd()==null){
            request.setEnd(System.currentTimeMillis());
        }
        //request.set
        if(request.getPage()>30){
            return null;
        }
        SearchRequestBuilder searchRequestBuilder = builder(request);

        searchRequestBuilder.highlighter(SearchSourceBuilder.highlight().preTags("<span class='highlight'>").postTags("</span>").field("message"));
        searchRequestBuilder.setFrom(request.getFrom()).setSize(request.getSize());
        searchRequestBuilder.addSort("timestamp", SortOrder.ASC);
        //
        SearchResponse response=searchRequestBuilder.execute().actionGet();
        response.getTook();
        LogQueryResult result = new LogQueryResult();
        SearchHit[] hits=response.getHits().getHits();
        List<SkyeLogEntity> logs=new LinkedList<>();
        for(SearchHit hit:hits){
            SkyeLogEntity entity = JacksonConvert.readValue(hit.getSourceAsString(),SkyeLogEntity.class);
            //reset the highlighted text
            if(hit.getHighlightFields()!=null&&hit.getHighlightFields().get("message")!=null){
                String highlighText = Arrays.toString(hit.getHighlightFields().get("message").getFragments());
                entity.setMessage(highlighText.substring(1,highlighText.length()-1));
            }
            logs.add(entity);
        }
        //resultBuilder.content(entities);
        //resultBuilder.total((int) response.getHits().totalHits);
        result.setLogs(logs);
        return result;
    }


    SearchRequestBuilder builder(LogSearchRequest request){
        List<String> indices=indexNameFormatter.formatTypeAndRange(null,request.getStart(),request.getEnd());
        String[] indicess=new String[indices.size()];
        indices.toArray(indicess);
        SearchRequestBuilder searchRequestBuilder=transportClient.prepareSearch(indicess)
                .setIndicesOptions(DefaultOptions.defaultIndicesOptions());
        BoolQueryBuilder root= QueryBuilders.boolQuery();
        root.filter(QueryBuilders.rangeQuery("timestamp")
                .gt(request.getStart()).lte(request.getEnd()));
        if(StringUtils.hasText(request.getService())){
            root.filter(QueryBuilders.termsQuery("serviceName",request.getService()));
        }
        if(!CollectionUtils.isEmpty(request.getLevel())){
            root.filter(QueryBuilders.termsQuery("level",request.getLevel()));
        }
        if (!Strings.isNullOrEmpty(request.traceId)) {
            root.filter(QueryBuilders.termsQuery("traceId", request.traceId));
        }
        if(!Strings.isNullOrEmpty(request.getKeyword())){
            //terms 查询是单词项精准查询，存储时使用了标准分析器，会统一成小写词项倒排索引中
            //短语搜索
            root.filter(QueryBuilders.matchPhraseQuery("message",request.getKeyword()));
        }
        if(!Strings.isNullOrEmpty(request.getMdc())){
            JavaType javaType= JacksonConvert.mapper()
                    .getTypeFactory().constructMapType(HashMap.class,String.class,String.class);
            Map<String,String> mdcMap=(Map<String, String>) JacksonConvert.readValue(request.getMdc(),javaType);
            for(Map.Entry entry:mdcMap.entrySet()){
                TermsQueryBuilder childTermsQuery=QueryBuilders.termsQuery(entry.getKey().toString()
                        ,entry.getValue().toString());
                root.filter(QueryBuilders.nestedQuery("mdc",childTermsQuery, ScoreMode.None));
            }
        }
        searchRequestBuilder.setQuery(root);
        return searchRequestBuilder;
    }

    /**
     * 50 个点
     * @param request
     * @return
     */
    @Override
    public List<CountMetric> countSeries(LogSearchRequest request) {

        SearchRequestBuilder searchRequestBuilder = builder(request);
        searchRequestBuilder.addAggregation(AggregationBuilders.dateHistogram("counts").field("timestamp")
                    .dateHistogramInterval(new DateHistogramInterval(request.getTimeInterval())).timeZone(DateTimeZone.forID("Asia/Shanghai")));

        SearchResponse response=searchRequestBuilder.execute().actionGet();
        Histogram counts  = response.getAggregations().get("counts");
        List<CountMetric> result = new ArrayList<>(counts.getBuckets().size());
        for(Histogram.Bucket bucket : counts.getBuckets()){
            result.add(new CountMetric(Long.parseLong(bucket.getKeyAsString()),bucket.getDocCount()));
        }
        return result;
    }


}
