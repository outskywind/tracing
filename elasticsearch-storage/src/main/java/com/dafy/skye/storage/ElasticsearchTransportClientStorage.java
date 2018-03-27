package com.dafy.skye.storage;

import com.dafy.skye.storage.zipkincopy.LegacyElasticsearchSpanStore;
import zipkin.internal.Nullable;
import zipkin.internal.V2StorageComponent;
import zipkin.storage.AsyncSpanStore;
import zipkin2.CheckResult;
import zipkin2.elasticsearch.ElasticsearchStorage;
import zipkin2.storage.SpanConsumer;
import zipkin2.storage.SpanStore;
import zipkin2.storage.StorageComponent;

import java.io.IOException;

/**
 * Created by quanchengyun on 2018/3/13.
 */
/**
 * 基于 transportClient 实现，storage级别涉及到新旧span格式转换兼容性
 * 直接抄源代码
 * {@see ElasticsearchHttpStorage}
 */
@Deprecated
public class ElasticsearchTransportClientStorage extends StorageComponent implements V2StorageComponent.LegacySpanStoreProvider{


    //这个代理 只是用来处理zipkin内部的数据结构
    private  ElasticsearchStorage delegate;
    final boolean legacyReadsEnabled, searchEnabled;

    ElasticsearchTransportClientStorage(ElasticsearchStorage delegate, boolean legacyReadsEnabled,
                             boolean searchEnabled) {
        this.delegate = delegate;
        this.legacyReadsEnabled = legacyReadsEnabled;
        this.searchEnabled = searchEnabled;
    }

    @Override public SpanStore spanStore() {
        return delegate.spanStore();
    }

    /**
     *
     * this is what we need to overwrite to use  elasticsearch TransportClient
     * @return
     */
    @Override public SpanConsumer spanConsumer() {
        //return delegate.spanConsumer();
        //TODO
        delegate.spanConsumer();

        return null;
    }

    @Override @Nullable
    public AsyncSpanStore legacyAsyncSpanStore() {
        if (!legacyReadsEnabled) return null;
        if (delegate.version() >= 6 /* multi-type (legacy) index isn't possible */) {
            return null;
        }
        return new LegacyElasticsearchSpanStore(delegate);
    }

    @Override public CheckResult check() {
        return delegate.check();
    }

    /** This is a blocking call, only used in tests. */
    void clear() throws IOException {
        delegate.clear();
    }

    @Override public void close() {
        delegate.close();
    }


    public static class Builder extends StorageComponent.Builder {
        private boolean legacyReadsEnabled,searchEnabled;

        public Builder() {
        }

        @Override
        public StorageComponent.Builder strictTraceId(boolean strictTraceId) {
            return null;
        }

        public StorageComponent.Builder legacyReadsEnabled(boolean legacyReadsEnabled){
            this.legacyReadsEnabled = legacyReadsEnabled;
            return this;
        }

        public StorageComponent.Builder searchEnabled(boolean searchEnabled){
            this.searchEnabled = searchEnabled;
            return this;
        }

        @Override
        public ElasticsearchTransportClientStorage build() {
            return  new ElasticsearchTransportClientStorage(ElasticsearchStorage.newBuilder().build(), legacyReadsEnabled, searchEnabled);
        }
    }



}
