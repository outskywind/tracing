package com.dafy.skye.brave.dubbo;

import brave.Span;
import brave.Tracer;
import brave.Tracing;
import brave.internal.Platform;
import brave.propagation.Propagation;
import brave.propagation.TraceContext;
import brave.propagation.TraceContextOrSamplingFlags;
import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.*;
import com.alibaba.dubbo.rpc.protocol.dubbo.FutureAdapter;
import com.alibaba.dubbo.rpc.support.RpcUtils;
import com.dafy.skye.brave.ConstantsBrave;
import org.slf4j.MDC;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * copy from brave.dubbo.rpc.TracingFilter
 * customize the span tag
 * Created by Caedmon on 2017/4/11.
 */
@Activate(group = {Constants.PROVIDER, Constants.CONSUMER})
public class DubboTracingFilter implements Filter{

    Tracer tracer;
    TraceContext.Extractor<Map<String, String>> extractor;
    TraceContext.Injector<Map<String, String>> injector;

    public void setTracing(Tracing tracing) {
        tracer = tracing.tracer();
        extractor = tracing.propagation().extractor(GETTER);
        injector = tracing.propagation().injector(SETTER);
    }


    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        if(tracer==null){return invoker.invoke(invocation);}
        //ClientRequestInterceptor clientRequestInterceptor=brave.clientRequestInterceptor();
        //ClientResponseInterceptor clientResponseInterceptor=brave.clientResponseInterceptor();
        //ClientSpanThreadBinder clientSpanThreadBinder=brave.clientSpanThreadBinder();
        //clientRequestInterceptor.handle(new DubboClientRequestAdapter(invoker,invocation));

        RpcContext rpcContext = RpcContext.getContext();
        Span.Kind kind = rpcContext.isProviderSide() ? Span.Kind.SERVER : Span.Kind.CLIENT;
        final Span span;
        if (kind.equals(Span.Kind.CLIENT)) {
            span = tracer.nextSpan();
            injector.inject(span.context(), invocation.getAttachments());
        } else {
            TraceContextOrSamplingFlags extracted = extractor.extract(invocation.getAttachments());
            span = extracted.context() != null
                    ? tracer.joinSpan(extracted.context())
                    : tracer.nextSpan(extracted);
        }
        span.name(DubboBraveHelper.getSpanName(rpcContext));
        //customize put traceId to MDC in server side
        if(kind.equals(Span.Kind.SERVER)){
            MDC.put(ConstantsBrave.MDC_TRACE_ID_KEY,Long.toHexString(span.context().traceId()));
        }
        if (!span.isNoop()) {
            span.kind(kind);
            String service = invoker.getInterface().getSimpleName();
            String method = RpcUtils.getMethodName(invocation);
            span.name(service + "." + method);
            parseRemoteAddress(rpcContext, span);
            span.start();
        }

        boolean isOneway = false, deferFinish = false;
        try (Tracer.SpanInScope scope = tracer.withSpanInScope(span)) {
            Result result = invoker.invoke(invocation);
            if (result.hasException()) {
                onError(result.getException(), span);
            }else {
                //
                onSuccess(span);
            }
            isOneway = RpcUtils.isOneway(invoker.getUrl(), invocation);
            Future<Object> future = rpcContext.getFuture(); // the case on async client invocation
            if (future instanceof FutureAdapter) {
                deferFinish = true;
                ((FutureAdapter) future).getFuture().setCallback(new FinishSpanCallback(span));
            }
            return result;
        } catch (Error | RuntimeException e) {
            onError(e, span);
            throw e;
        } finally {
            if(kind.equals(Span.Kind.SERVER)){
                MDC.remove(ConstantsBrave.MDC_TRACE_ID_KEY);
            }
            if (isOneway) {
                span.flush();
            } else if (!deferFinish) {
                span.finish();
            }
        }
    }

    protected  void parseRemoteAddress(RpcContext rpcContext, Span span) {
        InetSocketAddress remoteAddress = rpcContext.getRemoteAddress();
        if (remoteAddress == null) return;
        span.remoteIpAndPort(Platform.get().getHostString(remoteAddress), remoteAddress.getPort());
    }

    protected  void onError(Throwable error, Span span) {
        span.error(error);
        if (error instanceof RpcException) {
            span.tag("dubbo.error_code", Integer.toString(((RpcException) error).getCode()));
        }
    }

    protected  void onSuccess(Span span) {
        span.tag("status",ConstantsBrave.SUCCESS);
    }

    static final Propagation.Getter<Map<String, String>, String> GETTER =
            new Propagation.Getter<Map<String, String>, String>() {
                @Override
                public String get(Map<String, String> carrier, String key) {
                    return carrier.get(key);
                }

                @Override
                public String toString() {
                    return "Map::get";
                }
            };

    static final Propagation.Setter<Map<String, String>, String> SETTER =
            new Propagation.Setter<Map<String, String>, String>() {
                @Override
                public void put(Map<String, String> carrier, String key, String value) {
                    carrier.put(key, value);
                }

                @Override
                public String toString() {
                    return "Map::set";
                }
            };

}
