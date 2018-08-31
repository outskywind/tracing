package com.dafy.skye.brave.dubbo;

import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.RpcContext;
import com.github.kristofa.brave.*;

import java.net.InetSocketAddress;

import java.util.Collection;
import java.util.Collections;


/**
 * Created by Caedmon on 2017/4/11.
 */
public class DubboServerRequestAdapter implements ServerRequestAdapter{
    private Invoker invoker;
    private Invocation invocation;
    private ServerTracer tracer;
    public DubboServerRequestAdapter(Invoker<?> invoker, Invocation invocation, ServerTracer tracer){
        this.invoker=invoker;
        this.invocation=invocation;
        this.tracer=tracer;
    }

    public TraceData getTraceData() {
        TraceData defaultReturn=TraceData.EMPTY;
        String sampled =   invocation.getAttachment("sampled");
        if(sampled != null && sampled.equals("0")){
            return TraceData.NOT_SAMPLED;
        }else {
            final String parentId = invocation.getAttachment("parentId");
            final String spanId = invocation.getAttachment("spanId");
            final String traceId = invocation.getAttachment("traceId");
            if (traceId != null && spanId != null) {
                SpanId span = getSpanId(traceId, spanId, parentId);
                return TraceData.create(span);
            }
        }
        return defaultReturn;
    }

    public String getSpanName() {
        return DubboBraveHelper.getCurrentSpanName();
    }

    public Collection<KeyValueAnnotation> requestAnnotations() {
        //String ipAddr = RpcContext.getContext().getUrl().getIp();
        //InetSocketAddress inetSocketAddress = RpcContext.getContext().getRemoteAddress();
        //final String clientName = RpcContext.getContext().getUrl().getParameter("clientName");
        InetSocketAddress socketAddress = RpcContext.getContext().getLocalAddress();
        if (socketAddress != null) {
            KeyValueAnnotation ra = KeyValueAnnotation.create("address", socketAddress.toString());
            return Collections.singletonList(ra);
        } else {
            return Collections.emptyList();
        }
    }
    static SpanId getSpanId(String traceId, String spanId, String parentSpanId) {
        return SpanId.builder()
                .traceId(IdConversion.convertToLong(traceId))
                .spanId(IdConversion.convertToLong(spanId))
                .parentId(parentSpanId == null ? null : IdConversion.convertToLong(parentSpanId)).build();
    }
}
