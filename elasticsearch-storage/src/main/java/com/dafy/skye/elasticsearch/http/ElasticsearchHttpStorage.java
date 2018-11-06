package com.dafy.skye.elasticsearch.http;


import com.dafy.skye.zipkin.IndexNameFormatter;
import com.dafy.skye.zipkin.extend.CustomSpanStore;
import com.dafy.skye.zipkin.zipkincopy.ElasticsearchSpanConsumer;
import com.dafy.skye.zipkin.zipkincopy.EnsureIndexTemplate;
import com.dafy.skye.zipkin.zipkincopy.LegacyElasticsearchSpanStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zipkin.internal.Nullable;
import zipkin.internal.V2StorageComponent;
import zipkin.storage.AsyncSpanStore;
import zipkin2.elasticsearch.ElasticsearchStorage;
import zipkin2.storage.SpanConsumer;
import zipkin2.storage.SpanStore;
import zipkin2.storage.StorageComponent;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by quanchengyun on 2018/3/14.
 * 相关基础抽象类都是包私有的,copy扩展
 */
public class ElasticsearchHttpStorage extends StorageComponent implements V2StorageComponent.LegacySpanStoreProvider{

    private static final Logger log = LoggerFactory.getLogger(ElasticsearchHttpStorage.class);

    //这个代理用来处理zipkin内部的数据结构
    private ElasticsearchStorage delegate;
    private boolean legacyReadsEnabled, searchEnabled;
    private String indexTemplateSpan ;
    private volatile AtomicBoolean hasIndexTemplateSpan = new AtomicBoolean(false);

    private SpanConsumer spanConsumer ;

    private SpanStore spanStore;

    private IndexNameFormatter indexNameFormatter;

    public ElasticsearchHttpStorage(ElasticsearchStorage delegate, boolean legacyReadsEnabled,
                                        boolean searchEnabled,String indexTemplateSpan,IndexNameFormatter indexNameFormatter) {
        this.delegate = delegate;
        this.legacyReadsEnabled = legacyReadsEnabled;
        this.searchEnabled = searchEnabled;
        this.indexTemplateSpan = indexTemplateSpan;
        this.indexNameFormatter = indexNameFormatter;
        this.spanStore = new CustomSpanStore(this.delegate,searchEnabled,indexNameFormatter);
    }

    @Override public SpanStore spanStore() {
        return this.spanStore;
    }

    public void setSpanConsumer(SpanConsumer spanConsumer){
        this.spanConsumer = spanConsumer;
    }

    /**
     * this is what we need to overwrite to customize  elasticsearch
     * @return
      */
    @Override public SpanConsumer spanConsumer() {
        ensureIndexTemplates();
        if(this.spanConsumer==null){
            ElasticsearchSpanConsumer consumer =  new ElasticsearchSpanConsumer(delegate,this.searchEnabled);
            consumer.setIndexNameFormatter(indexNameFormatter);
            this.spanConsumer = consumer;
        }
        return this.spanConsumer;
    }
    @Override @Nullable
    public AsyncSpanStore legacyAsyncSpanStore() {
        if (!legacyReadsEnabled) return null;
        if (delegate.version() >= 6 /* multi-type (legacy) index isn't possible */) {
            return null;
        }
        return new LegacyElasticsearchSpanStore(delegate);
    }


    /**
     * 为了提高 时间序列 数据的聚合效率。默认的模板设置在第一次查询时很耗时，会超过30s，7天之内的数据
     * 设置索引模板，只要在索引创建之前设置一次即可，在默认的模板基础上进行一些优化调整
     */
    public void ensureIndexTemplates() {
        if(hasIndexTemplateSpan.get()){
            return;
        }
        //需要阻塞所有的请求，直到其中一个完成
        synchronized (hasIndexTemplateSpan){
            if(hasIndexTemplateSpan.compareAndSet(false,true)){
                String index = delegate.indexNameFormatter().index();
                try{
                    String indexTemplate = resolveIndexTemplate();
                    if(indexTemplate!=null){
                        log.info("zipkin index template load successfully");
                        EnsureIndexTemplate.apply(delegate.http(),index,indexTemplate);
                    }
                }catch (Exception e){
                    log.error("创建索引模板失败",e);
                    throw new RuntimeException(e);
                }
            }
        }
    }


    protected String  resolveIndexTemplate() {
        //System.out.println("this system classpath="+System.getProperty("java.class.path"));
        if(indexTemplateSpan!=null){
            //果然是在所有的classpath 下面加载资源文件。
            InputStream in  = this.getClass().getClassLoader().getResourceAsStream(indexTemplateSpan);
            if(in==null){
                throw new RuntimeException("index template " + indexTemplateSpan + "can't be load from classpath");
            }
            try{
                //since this is a local file inputStream read all bytes at once
                byte[] b = new byte[in.available()];
                String json;
                if(in.read(b)>0){
                    json = new String(b,"UTF-8");
                    return json;
                }
            }catch(IOException e){
                log.error("索引配置文件读取失败",e);
            }
            try{
                in.close();
            }catch (Exception e){
                //
            }
        }
        return null;
    }
}
