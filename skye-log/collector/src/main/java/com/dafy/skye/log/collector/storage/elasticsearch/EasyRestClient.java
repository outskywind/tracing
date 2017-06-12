package com.dafy.skye.log.collector.storage.elasticsearch;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.nio.entity.NStringEntity;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseListener;
import org.elasticsearch.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

/**
 * Created by Caedmon on 2017/4/28.
 * 基于RestClient的简单封装
 */
public class EasyRestClient {
    private RestClient restClient;
    private static final Logger log= LoggerFactory.getLogger(EasyRestClient.class);
    public EasyRestClient(RestClient restClient){
        this.restClient=restClient;
    }
    public Response request(String method,String endpoint){
        try {
            return restClient.performRequest(method,endpoint);
        } catch (IOException e) {
            throw new RestRuntimeException(method,endpoint,e);
        }
    }
    /**
     * 同步HTTP请求
     * @param builder request构造器
     * @return 相应结果
     * */
    public Response request(EasyRequestBuilder builder){
        HttpEntity httpEntity=null;
        if(!Strings.isNullOrEmpty(builder.body)){
            httpEntity=new NStringEntity(builder.body, Charsets.UTF_8);
        }
        Map<String,String> params=Collections.EMPTY_MAP;
        if(builder.params!=null&&!builder.params.isEmpty()){
            params=builder.params;
        }
        final String method=builder.method;
        final String endpoint=builder.endpoint;
        Response response=null;
        try {
            if(builder.headers==null){
                response=restClient.performRequest(method,endpoint,params,httpEntity);
            }else{
                response=restClient.performRequest(method,endpoint,params,httpEntity,builder.headers);
            }
            return response;
        } catch (IOException e) {
            throw new RestRuntimeException(method,endpoint,e);
        }
    }
    /**
     * 异步HTTP请求
     * @param builder request构造器
     * @param listener 异步结果回调监听器
     * */
    public void requestAsync(EasyRequestBuilder builder, ResponseListener listener){
        HttpEntity httpEntity=null;
        if(!Strings.isNullOrEmpty(builder.body)){
            httpEntity=new NStringEntity(builder.body, Charsets.UTF_8);
        }
        Map<String,String> params=Collections.EMPTY_MAP;
        if(builder.params!=null&&!builder.params.isEmpty()){
            params=builder.params;
        }
        final String method=builder.method;
        final String endpoint=builder.endpoint;
        if(builder.headers==null){
            restClient.performRequestAsync(method,endpoint,params,httpEntity,listener);
        }else{
            restClient.performRequestAsync(method,endpoint,params,httpEntity,listener,builder.headers);
        }

    }
    public void close(){
        try {
            restClient.close();
        } catch (IOException e) {
            log.error("EasyRestClient close error",e);
        }
    }
    public static class EasyRequestBuilder {
        private String method;
        private String endpoint;
        private Map<String,String> params=Collections.EMPTY_MAP;
        private String body;
        private Header[] headers;
        public EasyRequestBuilder method(String method){
            this.method=method;
            return this;
        }
        public EasyRequestBuilder enpoint(String endpoint){
            this.endpoint=endpoint;
            return this;
        }
        public EasyRequestBuilder params(Map<String,String> params){
            this.params=params;
            return this;
        }
        public EasyRequestBuilder body(String body){
            this.body=body;
            return this;
        }
        public EasyRequestBuilder headers(Header... headers){
            this.headers=headers;
            return this;
        }
    }
}
