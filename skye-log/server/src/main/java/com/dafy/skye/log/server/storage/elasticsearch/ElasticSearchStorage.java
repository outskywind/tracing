package com.dafy.skye.log.server.storage.elasticsearch;

import com.dafy.skye.common.elasticsearch.DefaultOptions;
import com.dafy.skye.common.util.IndexNameFormatter;
import com.dafy.skye.common.util.JacksonConvert;
import com.dafy.skye.common.util.ResourceUtil;
import com.dafy.skye.log.core.logback.SkyeLogEvent;
import com.dafy.skye.log.server.autoconfig.LogStorageESConfigProperties;
import com.dafy.skye.log.server.storage.StorageComponent;
import com.dafy.skye.log.server.storage.entity.SkyeLogEntity;
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
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.VersionType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

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
                    .addTransportAddress(new InetSocketTransportAddress(
                            InetAddress.getByName(array[0]), Integer.parseInt(array[1])));
        }
        IndexNameFormatter.Builder formatterBuilder=IndexNameFormatter.builder();
        formatterBuilder.dateSeparator('-');
        final String index=esConfig.getIndex();
        this.indexNameFormatter=formatterBuilder.index(index).build();
    }
    /**
     * 确认模版是否已存在,如果模版不存在则创建一个
     * */
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
        Settings.Builder settings=Settings.builder().put("number_of_shards",esConfig.getIndexShards());
        settings.put("number_of_replicas",1);
        requestBuilder.setSettings(settings.build());
        requestBuilder.setTemplate(esConfig.getIndex()+"-*");
        String mappingSource=ResourceUtil.readString("elasticsearch-template.json");
        requestBuilder.addMapping(esConfig.getType(),mappingSource, XContentType.JSON);
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
        String index=indexNameFormatter.indexNameForTimestamp(event.getTimeStamp());
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
            String index=indexNameFormatter.indexNameForTimestamp(event.getTimeStamp());
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
            for (int i = 0; i < responses.getItems().length; i++) {
                BulkItemResponse response = responses.getItems()[i];
                if (!response.isFailed()) {
                    return;
                }
            }
            throw new Exception("Message all failed save to es");
        }
    }
    @Override
    public LogQueryResult query(LogSearchRequest request) {
        if(request.endTs==null){
            request.endTs=System.currentTimeMillis();
        }
        if(request.lookback==null){
            request.lookback=Long.valueOf(24*3600*1000);
        }
        List<String> indices=indexNameFormatter.indexNamePatternsForRange(request.endTs-request.lookback,request.endTs);
        String[] indicess=new String[indices.size()];
        indices.toArray(indicess);
        SearchRequestBuilder searchRequestBuilder=transportClient.prepareSearch(indicess)
                .setIndicesOptions(DefaultOptions.defaultIndicesOptions());
        BoolQueryBuilder root= QueryBuilders.boolQuery();
        if(!CollectionUtils.isEmpty(request.serviceNames)){
            root.filter(QueryBuilders.termsQuery("serviceName",request.serviceNames));
        }
        if(!CollectionUtils.isEmpty(request.levels)){
            root.filter(QueryBuilders.termsQuery("level",request.levels));
        }
        if (!Strings.isNullOrEmpty(request.traceId)) {
            root.filter(QueryBuilders.termsQuery("traceId", request.traceId));
        }
        if(!Strings.isNullOrEmpty(request.message)){
            //terms 查询是单词项精准查询，存储时使用了标准分析器，会统一成小写词项倒排索引中
            root.filter(QueryBuilders.matchQuery("message",request.message).operator(Operator.AND));
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
        searchRequestBuilder.setFrom(request.getFrom()).setSize(request.getPageSize());
        searchRequestBuilder.addSort("tsUuid", SortOrder.DESC);
        SearchResponse response=searchRequestBuilder.execute().actionGet();
        response.getTook();
        LogQueryResult.Builder resultBuilder=LogQueryResult.newBuilder();
        resultBuilder.took(response.getTookInMillis());
        resultBuilder.error(response.getShardFailures().toString());
        SearchHit[] hits=response.getHits().getHits();
        List<SkyeLogEntity> entities=new LinkedList<>();
        for(SearchHit hit:hits){
            entities.add(JacksonConvert.readValue(hit.getSourceAsString(),SkyeLogEntity.class));
        }
        resultBuilder.content(entities);
        resultBuilder.total((int) response.getHits().totalHits);
        return resultBuilder.build();
    }

    @Override
    public Set<String> getServices() {
        long endTs=System.currentTimeMillis();
        long startTs=System.currentTimeMillis()-esConfig.getDefaultLookback();
        List<String> indices=indexNameFormatter.indexNamePatternsForRange(startTs,endTs);
        String[] indicess=new String[indices.size()];
        indices.toArray(indicess);
        SearchRequestBuilder searchRequestBuilder=transportClient.prepareSearch(indicess)
                .setIndicesOptions(DefaultOptions.defaultIndicesOptions());
        searchRequestBuilder.addAggregation(AggregationBuilders.terms("service_terms")
                .field("serviceName").size(200));
        searchRequestBuilder.setSize(0);
        SearchResponse response=searchRequestBuilder.execute().actionGet();
        Aggregations aggregations=response.getAggregations();
        Set<String> services=new HashSet<>();
        if(aggregations!=null){
            Terms terms=aggregations.get("service_terms");
            for(Terms.Bucket bucket:terms.getBuckets()){
                String service=bucket.getKeyAsString();
                services.add(service);
            }
        }

        return services;
    }

}
