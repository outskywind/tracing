/*
package com.dafy.skye.brave.dubbo;

import brave.Span;
import brave.Tracing;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.*;
import com.dafy.skye.brave.ConstantsBrave;
import org.slf4j.MDC;

*/
/**
 * Created by Caedmon on 2017/4/11.
 *//*

@Activate(
        group ={"provider"}
)
public class DubboBraveProviderFilter implements Filter {
    private volatile Tracing tracing;
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        //由于客户端没有正确配置监控信息，将不会拦截统计调用链上报
        if(tracing==null){
            return invoker.invoke(invocation);
        }
        else{
            //如果是外部没有传递接入监控
            //String traceId=invocation.getAttachment("traceId");
            //ServerRequestInterceptor serverRequestInterceptor=brave.serverRequestInterceptor();
            //ServerResponseInterceptor serverResponseInterceptor=brave.serverResponseInterceptor();

            //serverRequestInterceptor.handle(new DubboServerRequestAdapter(invoker,invocation,brave.serverTracer()));
            String spanName = DubboBraveHelper.getCurrentSpanName();
            Span span = tracing.tracer().nextSpan().name(spanName);
            span.start();
            //put the traceId into MDC
            MDC.put(ConstantsBrave.MDC_TRACE_ID_KEY,Long.toHexString(span.context().traceId()));
            try{
                Result result = invoker.invoke(invocation);
                //serverResponseInterceptor.handle(new DubboServerResponseAdapter(result));
                if(result.hasException()){
                    span.tag("exception",result.getException().getMessage());
                }else{
                    span.tag("status","success");
                }
                return result;
            }catch(Exception e){
                //serverResponseInterceptor.handle(new DubboServerResponseAdapter(e));
                span.tag("exception",e.getMessage());
                throw  new RpcException(e);
            }finally {
                //请求处理完毕清除TraceId
                span.finish();
                MDC.remove(ConstantsBrave.MDC_TRACE_ID_KEY);
            }
        }
    }

    public void setTracing(Tracing tracing) {
        this.tracing = tracing;
    }
}
*/
