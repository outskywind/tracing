package com.dafy.skye.brave.dubbo;

import com.alibaba.dubbo.registry.integration.RegistryDirectory;
import com.alibaba.dubbo.registry.integration.RegistryProtocol;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.RpcContext;
import com.github.kristofa.brave.ClientRequestAdapter;
import com.github.kristofa.brave.IdConversion;
import com.github.kristofa.brave.KeyValueAnnotation;
import com.github.kristofa.brave.SpanId;
import com.twitter.zipkin.gen.Endpoint;
import org.slf4j.MDC;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by Caedmon on 2017/4/11.
 */
public class DubboClientRequestAdapter implements ClientRequestAdapter {
    private Invoker<?> invoker;
    private Invocation invocation;
    public DubboClientRequestAdapter(Invoker<?> invoker, Invocation invocation){
        this.invoker=invoker;
        this.invocation=invocation;
    }
    public String getSpanName() {
        return DubboBraveHelper.getCurrentSpanName();
    }
    public void addSpanIdToRequest(SpanId spanId) {
        boolean parentIdNull=spanId.nullableParentId()==null;
        String skyeTraceId;
        if(parentIdNull){
            skyeTraceId=spanId.traceIdString();
        }else{
            skyeTraceId=IdConversion.convertToString(spanId.parentId);
        }
        MDC.put(Constants.MDC_TRACE_ID_KEY,skyeTraceId);
        String application = RpcContext.getContext().getUrl().getParameter("application");
        RpcContext rpcContext=RpcContext.getContext();
        rpcContext.setAttachment("clientName", application);
        if (spanId == null) {
            rpcContext.setAttachment("sampled", "0");
        }else{
            rpcContext.setAttachment("traceId", IdConversion.convertToString(spanId.traceId));
            rpcContext.setAttachment("spanId", IdConversion.convertToString(spanId.spanId));
            if (spanId.nullableParentId() != null) {
                RpcContext.getContext().setAttachment("parentId", IdConversion.convertToString(spanId.parentId));
            }
        }
    }
    @Override
    public Collection<KeyValueAnnotation> requestAnnotations() {
        RpcContext context=RpcContext.getContext();
        KeyValueAnnotation arguments=KeyValueAnnotation.create("arguments",context.getArguments().toString());
        return Collections.singletonList(arguments);
    }
    @Override
    public Endpoint serverAddress() {
//        RpcContext rpcContext=RpcContext.getContext();
//        InetSocketAddress inetSocketAddress = RpcContext.getContext().getRemoteAddress();
//        String ip = rpcContext.getUrl().getIp();
//        Endpoint.Builder endpointBuilder= Endpoint.builder();
//        endpointBuilder.ipv4(DubboBraveHelper.convertToInt(ip));
//        endpointBuilder.port(inetSocketAddress.getPort());
//        endpointBuilder.serviceName("UNKNOWN");
//        return endpointBuilder.build();
        return null;
    }
}
