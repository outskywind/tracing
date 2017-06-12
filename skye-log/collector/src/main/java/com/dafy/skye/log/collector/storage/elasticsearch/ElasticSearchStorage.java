package com.dafy.skye.log.collector.storage.elasticsearch;

import com.dafy.skye.log.collector.storage.StorageComponent;
import com.dafy.skye.log.collector.storage.elasticsearch.internal.ESearchRequest;
import com.dafy.skye.log.collector.storage.elasticsearch.internal.ESearchResponse;
import com.dafy.skye.log.collector.storage.entity.SkyeLogEntity;
import com.dafy.skye.log.collector.storage.query.LogQueryRequest;
import com.dafy.skye.log.collector.storage.query.LogQueryResult;
import com.dafy.skye.log.collector.util.IndexNameFormatter;
import com.dafy.skye.log.collector.util.JacksonConvert;
import com.dafy.skye.log.collector.util.ResourceUtil;
import com.dafy.skye.log.core.logback.SkyeLogEvent;
import com.fasterxml.jackson.databind.JavaType;
import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.io.CharStreams;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseListener;
import org.elasticsearch.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Caedmon on 2017/4/26.
 */
public class ElasticSearchStorage implements StorageComponent {
    private EasyRestClient easyRestClient;
    private ElasticsearchStorageConfig esConfig;
    private IndexNameFormatter indexNameFormatter;
    private static final Logger log= LoggerFactory.getLogger(ElasticSearchStorage.class);
    public ElasticSearchStorage(ElasticsearchStorageConfig esConfig){
        this.esConfig = esConfig;
        HttpHost[] hosts=new HttpHost[esConfig.getNodes().size()];
        int i=0;
        for(String address: esConfig.getNodes()){
            String[] array=address.split(":");
            String host=array[0];
            int port=Integer.parseInt(array[1]);
            hosts[i]=new HttpHost(host,port);
            i++;
        }
        this.easyRestClient =new EasyRestClient(RestClient.builder(hosts).build());
        IndexNameFormatter.Builder formatterBuilder=IndexNameFormatter.builder();
        formatterBuilder.dateSeparator('-');
        this.indexNameFormatter=formatterBuilder.index(esConfig.getIndex()).build();

    }
    /**
     * 确认模版是否已存在,如果模版不存在则创建一个
     * */
    boolean ensureTemplate(){
        final String index=esConfig.getIndex();
        Response response=easyRestClient.request("HEAD","/_template/"+index+"_template");
        StatusLine line=response.getStatusLine();
        //not exists
        if(line.getStatusCode()==HttpStatus.SC_NOT_FOUND){
            EasyRestClient.EasyRequestBuilder builder= new EasyRestClient.EasyRequestBuilder();
            builder.method("PUT").enpoint("/_template/"+index+"_template");
            String templateBody= ResourceUtil.readString("elasticsearch-template.json");
            String afterReplace=templateBody.replace("${__INDEX__}",index)
            .replace("${__NUMBER_OF_SHARDS__}",String.valueOf(esConfig.getIndexShards()))
            .replace("${__NUMBER_OF_REPLICAS__}",String.valueOf(esConfig.getIndexReplicas()));
            builder.body(afterReplace);
            Response putResponse=easyRestClient.request(builder);
            if(putResponse.getStatusLine().getStatusCode()==HttpStatus.SC_OK){
                log.info("Put elasticSearch template success");
                return true;
            }else{
                log.error("Put elasticSearch Template Fail");
                return false;
            }
        }else if(line.getStatusCode()==HttpStatus.SC_OK){
            log.info("ElasticSearch template has already exists");
            return true;
        }else{
            log.error("Ensure template error:status={}",line.getStatusCode());
            return false;
        }
    }
    @Override
    public void start() {
        log.info("ElasticSearch Storage started");
        ensureTemplate();
    }

    @Override
    public void stop() {
        easyRestClient.close();
    }

    @Override
    public void save(final SkyeLogEvent event) {
        final String type= esConfig.getType();
        String logIndex=indexNameFormatter.indexNameForTimestamp(event.getTimeStamp());
        final String endpoint="/"+logIndex+"/"+type;
        EasyRestClient.EasyRequestBuilder builder=new EasyRestClient.EasyRequestBuilder();
        final SkyeLogEntity entity=SkyeLogEntity.build(event);
        builder.method("POST").enpoint(endpoint).body(JacksonConvert.toJsonString(entity));
         easyRestClient.requestAsync(builder, new ResponseListener() {
            @Override
            public void onSuccess(Response response) {
                int status=response.getStatusLine().getStatusCode();
                if(status>=HttpStatus.SC_OK&&status<=HttpStatus.SC_MULTIPLE_CHOICES){
                    log.debug("Save log success:traceId={}",entity.getTraceId());
                }else{
                    log.warn("Save log something wrong:status={},traceId={}",status,entity.getTraceId());
                }
            }

            @Override
            public void onFailure(Exception exception) {
                log.error("Save log error:traceId={}",entity.getTraceId(),exception);
                //TODO 存入ES失败的日志,需要备份存储,除非能保证collector的高可用
            }
        });

    }

    @Override
    public void batchSave(Collection<SkyeLogEvent> events) {
        final String typeName= esConfig.getType();
        StringBuilder body=new StringBuilder();
        for(SkyeLogEvent event:events){
            SkyeLogEntity entity=SkyeLogEntity.build(event);
            String logIndexName=indexNameFormatter.indexNameForTimestamp(event.getTimeStamp());
            String indexMetadata=indexMetadata(logIndexName,typeName);
            String documentSource= JacksonConvert.toJsonString(entity)+"\n";
            body.append(indexMetadata).append(documentSource);
        }
        EasyRestClient.EasyRequestBuilder builder=new EasyRestClient.EasyRequestBuilder();
        builder.method("POST").enpoint("/_bulk").body(body.toString());
        easyRestClient.requestAsync(builder, new ResponseListener() {
            @Override
            public void onSuccess(Response response) {
                int status=response.getStatusLine().getStatusCode();
                if(status==HttpStatus.SC_OK){
                    log.debug("BatchSave log success");
                }else{
                    log.warn("BatchSave log something wrong:status={}");
                }
            }

            @Override
            public void onFailure(Exception exception) {
                log.error("BatchSave log error",exception);
            }
        });
    }
    String indexMetadata(String indexName,String typeName){
        StringBuilder builder=new StringBuilder();
        builder.append("{\"index\":{\"_index\":\"").append(indexName).append('"');
        builder.append(",\"_type\":\"").append(typeName).append('"');
        builder.append("}}\n");
        return builder.toString();
    }
    @Override
    public LogQueryResult query(LogQueryRequest request) {
        SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String begin=dateFormat.format(request.getStartTs());
        String end=dateFormat.format(request.getEndTs());
        List<String> indices=indexNameFormatter.indexNamePatternsForRange(request.getStartTs(),request.getEndTs());
        ESearchRequest searchRequest= ESearchRequest.forIndicesAndType(indices,esConfig.getType());
        ESearchRequest.Filters filters = new ESearchRequest.Filters();
        filters.addDateRange("timestamp", begin, end,"yyyy-MM-dd HH:mm:ss.SSS");
        if (request.getServiceNames() != null&&!request.getServiceNames().isEmpty()) {
            filters.addTerms("serviceName",request.getServiceNames());
        }
        if(request.getLevels()!=null&&!request.getLevels().isEmpty()){
            filters.addTerms("level",request.getLevels());
        }
        if (!Strings.isNullOrEmpty(request.getTraceId())) {
            filters.addTerm("traceId", request.getTraceId());
        }
        if(!Strings.isNullOrEmpty(request.getMessage())){
            filters.addTerm("message",request.getMessage());
        }
        if(!Strings.isNullOrEmpty(request.getMdc())){
            JavaType javaType=JacksonConvert.mapper()
                    .getTypeFactory().constructMapType(HashMap.class,String.class,String.class);
            Map<String,String> mdcMap=(Map<String, String>) JacksonConvert.readValue(request.getMdc(),javaType);
            Map<String,String> mdcNestedTerms=Maps.newHashMap();
            for(Map.Entry entry:mdcMap.entrySet()){
                mdcNestedTerms.put("mdc."+entry.getKey(),entry.getValue().toString());
            }
            filters.addNestedTerms(mdcNestedTerms);
        }
        searchRequest.filters(filters);
        searchRequest.addSort("tsUuid",new ESearchRequest.SortOrder("desc",null));
        EasyRestClient.EasyRequestBuilder builder= new EasyRestClient.EasyRequestBuilder();
        StringBuilder endpointBuilder=new StringBuilder();
        endpointBuilder.append("/").append(join(indices))
                .append("/")
                .append(esConfig.getType())
                .append("/_search");
        Map<String,String> params=new HashMap<>();
        params.put("allow_no_indices","true");
        params.put("expand_wildcards","open");
        params.put("ignore_unavailable","true");
        LogQueryResult.Builder resultBuilder=LogQueryResult.newBuilder();
        builder.method("GET").enpoint(endpointBuilder.toString())
                .params(params)
                .body(JacksonConvert.toJsonString(searchRequest));
        Response restResponse=easyRestClient.request(builder);
        int statusCode=restResponse.getStatusLine().getStatusCode();
        String respBody=null;
        try{
            InputStream inputStream=restResponse.getEntity().getContent();
            respBody=CharStreams.toString(new InputStreamReader(inputStream, Charsets.UTF_8));
        }catch (IOException e){
            resultBuilder.success(false);
            resultBuilder.error("Read http response error:"+e.getMessage());
            return resultBuilder.build();
        }
        JavaType javaType= JacksonConvert.mapper.getTypeFactory()
                .constructParametricType(ESearchResponse.class,SkyeLogEntity.class);
        ESearchResponse<SkyeLogEntity> response=(ESearchResponse<SkyeLogEntity>) JacksonConvert.readValue(respBody,javaType);
        response.setStatus(statusCode);
        if(!response.isSucces()){
            resultBuilder.success(false);
            resultBuilder.error(response.getError().toString());
            return resultBuilder.build();
        }else{
            resultBuilder.success(true);
            List<SkyeLogEntity> entities=new ArrayList<>(response.getHits().getTotal());
            List<ESearchResponse.Document<SkyeLogEntity>> documents=response.getHits().getHits();
            for(ESearchResponse.Document<SkyeLogEntity> document:documents){
                SkyeLogEntity entity=document.get_source();
                entities.add(entity);
            }
            resultBuilder.content(entities);
            resultBuilder.took(response.getTook());
            resultBuilder.total(response.getHits().getTotal());
            return resultBuilder.build();
        }
    }
    static String join(List<String> parts) {
        StringBuilder to = new StringBuilder();
        for (int i = 0, length = parts.size(); i < length; i++) {
            to.append(parts.get(i));
            if (i + 1 < length) {
                to.append(',');
            }
        }
        return to.toString();
    }
}
