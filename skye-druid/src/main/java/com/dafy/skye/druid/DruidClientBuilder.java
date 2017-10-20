package com.dafy.skye.druid;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.inject.*;
import com.metamx.common.lifecycle.Lifecycle;
import com.metamx.emitter.core.NoopEmitter;
import com.metamx.emitter.service.ServiceEmitter;
import com.metamx.http.client.HttpClient;
import com.metamx.http.client.HttpClientConfig;
import com.metamx.http.client.HttpClientInit;
import io.druid.client.DirectDruidClient;
import io.druid.guice.GuiceInjectors;
import io.druid.guice.JsonConfigProvider;
import io.druid.guice.annotations.Json;
import io.druid.guice.annotations.Self;
import io.druid.guice.annotations.Smile;
import io.druid.guice.http.DruidHttpClientConfig;
import io.druid.initialization.Initialization;
import io.druid.query.*;
import io.druid.server.DruidNode;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * druid-client 里使用的guice-inject 管理依赖
 * 不依赖于spring ioc
 */
public class DruidClientBuilder {

    private static  Injector INJECTOR;
    private static  QueryToolChestWarehouse WAREHOUSE;
    private static  QueryWatcher WATCHER;
    private static  ObjectMapper JSON_MAPPER;
    private static  ObjectMapper SMILE_MAPPER;
    private static  DruidHttpClientConfig HTTP_CLIENT_CONFIG;
    private static  ServiceEmitter SERVICE_EMITTER;


    static{

        INJECTOR = Initialization.makeInjectorWithModules(
                GuiceInjectors.makeStartupInjector(),
                ImmutableList.of(
                        new Module()
                        {
                            @Override
                            public void configure(Binder binder)
                            {
                                JsonConfigProvider.bindInstance(
                                        binder,
                                        Key.get(DruidNode.class, Self.class),
                                        new DruidNode("druid-client", null, null)
                                );
                            }
                        },
                        new Module()
                        {
                            @Override
                            public void configure(Binder binder)
                            {
                                binder.bind(new TypeLiteral<Map<Class<? extends Query>, QueryToolChest>>(){}).to(new TypeLiteral<HashMap<Class<? extends Query>, QueryToolChest>>(){});
                                binder.bind(QueryToolChestWarehouse.class).to(MapQueryToolChestWarehouse.class);
                                JsonConfigProvider.bind(binder, "druid.client.http", DruidHttpClientConfig.class);

                                // Set up dummy DruidProcessingConfig to avoid large offheap buffer generation.
                                final DruidProcessingConfig dummyConfig = new DruidProcessingConfig()
                                {
                                    @Override
                                    public int intermediateComputeSizeBytes()
                                    {
                                        return 1;
                                    }

                                    @Override
                                    public String getFormatString()
                                    {
                                        return "dummy";
                                    }
                                };
                                binder.bind(DruidProcessingConfig.class).toInstance(dummyConfig);
                            }
                        }
                )
        );
        WAREHOUSE = INJECTOR.getInstance(QueryToolChestWarehouse.class);
        WATCHER = new QueryWatcher()
        {
            @Override
            public void registerQuery(Query query, ListenableFuture future)
            {
            }
        };
        JSON_MAPPER = INJECTOR.getInstance(Key.get(ObjectMapper.class, Json.class));
        SMILE_MAPPER = INJECTOR.getInstance(Key.get(ObjectMapper.class, Smile.class));
        HTTP_CLIENT_CONFIG = INJECTOR.getInstance(DruidHttpClientConfig.class);
        SERVICE_EMITTER = new ServiceEmitter("druid-client", "localhost", new NoopEmitter());
    }

    public static DruidClient create(final String host, Lifecycle lifecycle)
    {
        return create(Arrays.asList(host),lifecycle);
    }

    public static DruidClient create(List<String> hosts, Lifecycle lifecycle)
    {
        HttpClientConfig.Builder builder = HttpClientConfig
                .builder()
                .withNumConnections(HTTP_CLIENT_CONFIG.getNumConnections())
                .withReadTimeout(HTTP_CLIENT_CONFIG.getReadTimeout());
        HttpClient httpClient = HttpClientInit.createClient(builder.build(), lifecycle);
        Map<String,DirectDruidClient> clients = new HashMap<>();
        for(String host:hosts){
            final DirectDruidClient directDruidClient = new DirectDruidClient(
                    WAREHOUSE,
                    WATCHER,
                    SMILE_MAPPER,
                    httpClient,
                    host,
                    SERVICE_EMITTER
            );
            clients.put(host,directDruidClient);
        }
        DruidClient druidClient = new DruidClient(clients,httpClient,lifecycle);
        druidClient.setJsonMapper(getJsonMapper());
        return druidClient;
    }

    /**
     * 添加新的broker连接
     * @param client
     * @param hosts
     */
    public static void addNewHost(DruidClient client,List<String> hosts){
        HttpClient httpClient = client.getHttpClient();
        if(httpClient==null){
            Lifecycle lifecycle = new Lifecycle();
            HttpClientConfig.Builder builder = HttpClientConfig
                    .builder()
                    .withNumConnections(HTTP_CLIENT_CONFIG.getNumConnections())
                    .withReadTimeout(HTTP_CLIENT_CONFIG.getReadTimeout());
            httpClient= HttpClientInit.createClient(builder.build(), lifecycle);
        }
        //
        for(String host:hosts){
            final DirectDruidClient directDruidClient = new DirectDruidClient(
                    WAREHOUSE,
                    WATCHER,
                    SMILE_MAPPER,
                    httpClient,
                    host,
                    SERVICE_EMITTER
            );
            client.cached_clients.put(host,directDruidClient);
        }
    }

    public static ObjectMapper getJsonMapper() {
        return JSON_MAPPER;
    }

    public static void setJsonMapper(ObjectMapper jsonMapper) {
        JSON_MAPPER = jsonMapper;
    }
}
