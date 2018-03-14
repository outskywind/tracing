package com.dafy.skye.storage.http;

import com.dafy.skye.storage.zipkincopy.ElasticsearchSpanConsumer;
import com.dafy.skye.storage.zipkincopy.EnsureIndexTemplate;
import com.dafy.skye.storage.zipkincopy.LegacyElasticsearchSpanStore;
import com.dafy.skye.storage.zipkincopy.PseudoAddressRecordSet;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zipkin.internal.Nullable;
import zipkin.internal.V2StorageComponent;
import zipkin.storage.AsyncSpanStore;
import zipkin2.elasticsearch.ElasticsearchStorage;
import zipkin2.elasticsearch.internal.client.HttpCall;
import zipkin2.internal.Platform;
import zipkin2.storage.SpanConsumer;
import zipkin2.storage.SpanStore;
import zipkin2.storage.StorageComponent;

import java.io.IOException;
import java.util.List;
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

    private OkHttpClient client ;

    public ElasticsearchHttpStorage(ElasticsearchStorage delegate, boolean legacyReadsEnabled,
                                        boolean searchEnabled,String indexTemplateSpan) {
        this.delegate = delegate;
        this.legacyReadsEnabled = legacyReadsEnabled;
        this.searchEnabled = searchEnabled;
        this.indexTemplateSpan = indexTemplateSpan;
    }

    @Override public SpanStore spanStore() {
        return delegate.spanStore();
    }
    /**
     * this is what we need to overwrite to customize  elasticsearch
     * @return
      */
    @Override public SpanConsumer spanConsumer() {
        //TODO
        //return delegate.spanConsumer();
        ensureIndexTemplates();
        return new ElasticsearchSpanConsumer(delegate,this.searchEnabled);
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
                    EnsureIndexTemplate.apply(http(),index,indexTemplateSpan);
                }catch (IOException e){
                    log.error("创建索引模板失败");
                    throw Platform.get().uncheckedIOException(e);
                }
            }
        }
    }

    public // hosts resolution might imply a network call, and we might make a new okhttp instance
    HttpCall.Factory http() {
        List<String> hosts = delegate.hostsSupplier().get();
        if (hosts.isEmpty()) throw new IllegalArgumentException("no hosts configured");
        OkHttpClient ok = hosts.size() == 1
                ? client
                : client.newBuilder()
                .dns(PseudoAddressRecordSet.create(hosts, client.dns()))
                .build();
        ok.dispatcher().setMaxRequests(1);
        ok.dispatcher().setMaxRequestsPerHost(1);
        return new HttpCall.Factory(ok, HttpUrl.parse(hosts.get(0)));
    }

    public OkHttpClient getClient() {
        return client;
    }

    public void setClient(OkHttpClient client) {
        this.client = client;
    }
}
