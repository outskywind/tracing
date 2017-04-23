package com.dafy.skye.brave.dubbo;

import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.*;
import com.github.kristofa.brave.Brave;
import com.github.kristofa.brave.ServerRequestInterceptor;
import com.github.kristofa.brave.ServerResponseInterceptor;
import org.slf4j.MDC;

/**
 * Created by Caedmon on 2017/4/11.
 */
@Activate(
        group ={"provider"}
)
public class DubboBraveProviderFilter implements Filter {
    private volatile Brave brave;
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        String traceId=invocation.getAttachment("traceId");
        MDC.put(Constants.MDC_TRACE_ID_KEY,traceId);
        ServerRequestInterceptor serverRequestInterceptor=brave.serverRequestInterceptor();
        ServerResponseInterceptor serverResponseInterceptor=brave.serverResponseInterceptor();
        serverRequestInterceptor.handle(new DubboServerRequestAdapter(invoker,invocation,brave.serverTracer()));
        Result result = invoker.invoke(invocation);
        serverResponseInterceptor.handle(new DubboServerResponseAdapter(result));
        return result;
    }

    public Brave getBrave() {
        return brave;
    }

    public void setBrave(Brave brave) {
        this.brave = brave;
    }
}
