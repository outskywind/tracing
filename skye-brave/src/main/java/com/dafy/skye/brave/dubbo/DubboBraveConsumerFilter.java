package com.dafy.skye.brave.dubbo;

import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.*;
import com.github.kristofa.brave.*;

/**
 * Created by Caedmon on 2017/4/11.
 */
@Activate(
        group = "consumer"
)
public class DubboBraveConsumerFilter implements Filter{

    private volatile Brave brave;
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        if(brave==null){
            //由于客户端没有正确配置监控信息，将不会拦截统计调用链上报
            return invoker.invoke(invocation);
        } else{

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
                //不能在client response后清除掉local span
                clientSpanThreadBinder.setCurrentSpan(null);
            }
        }
    }

    public Brave getBrave() {
        return brave;
    }

    public void setBrave(Brave brave) {
        this.brave = brave;
    }
}
