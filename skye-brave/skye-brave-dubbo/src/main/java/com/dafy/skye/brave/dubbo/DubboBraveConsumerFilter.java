package com.dafy.skye.brave.dubbo;

import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.*;
import com.github.kristofa.brave.Brave;
import com.github.kristofa.brave.ClientRequestInterceptor;
import com.github.kristofa.brave.ClientResponseInterceptor;
import com.github.kristofa.brave.ClientSpanThreadBinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Caedmon on 2017/4/11.
 */
@Activate(
        group = "consumer"
)
public class DubboBraveConsumerFilter implements Filter{
    private volatile Brave brave;
    private static final Logger log= LoggerFactory.getLogger(DubboBraveConsumerFilter.class);
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        ClientRequestInterceptor clientRequestInterceptor=brave.clientRequestInterceptor();
        ClientResponseInterceptor clientResponseInterceptor=brave.clientResponseInterceptor();
        ClientSpanThreadBinder clientSpanThreadBinder=brave.clientSpanThreadBinder();
        clientRequestInterceptor.handle(new DubboClientRequestAdapter(invoker,invocation));
        try{
            Result result = invoker.invoke(invocation);
            clientResponseInterceptor.handle(new DubboClientResponseAdapter(result));
            return result;
        }catch (Throwable ex){
            clientResponseInterceptor.handle(new DubboClientResponseAdapter(ex));
            throw  new RpcException(ex);
        }finally {
            clientSpanThreadBinder.setCurrentSpan(null);
        }
    }

    public Brave getBrave() {
        return brave;
    }

    public void setBrave(Brave brave) {
        this.brave = brave;
    }
}
