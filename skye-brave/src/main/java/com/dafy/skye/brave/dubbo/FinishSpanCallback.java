package com.dafy.skye.brave.dubbo;

import brave.Span;
import com.alibaba.dubbo.remoting.exchange.ResponseCallback;
import com.alibaba.dubbo.rpc.RpcException;

/**
 * Created by quanchengyun on 2018/10/13.
 */
public class FinishSpanCallback implements ResponseCallback {
    final Span span;

    FinishSpanCallback(Span span) {
        this.span = span;
    }

    @Override public void done(Object response) {
        //
        span.finish();
    }

    @Override public void caught(Throwable exception) {
        onError(exception, span);
        span.finish();
    }

    protected void onError(Throwable error, Span span) {
        span.error(error);
        if (error instanceof RpcException) {
            span.tag("dubbo.error_code", Integer.toString(((RpcException) error).getCode()));
        }
    }
}