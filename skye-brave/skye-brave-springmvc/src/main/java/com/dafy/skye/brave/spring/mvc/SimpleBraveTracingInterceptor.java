package com.dafy.skye.brave.spring.mvc;

import com.github.kristofa.brave.Brave;
import com.github.kristofa.brave.ServerRequestAdapter;
import com.github.kristofa.brave.ServerResponseAdapter;
import com.github.kristofa.brave.ServerResponseInterceptor;
import com.github.kristofa.brave.http.*;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URI;

/**
 * Created by Caedmon on 2017/6/27.
 *
 * traceId 用于监控调用链跟踪，但对于分布式日志收集系统来说不是必须的
 * 因此在这里负责放入 MDC// server 端请求开始设置，结束清除
 */
public class SimpleBraveTracingInterceptor extends HandlerInterceptorAdapter{
    private Brave brave;
    public SimpleBraveTracingInterceptor(Brave brave){
        this.brave=brave;
    }
    @Override
    public boolean preHandle(final HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpServerRequest serverRequest=new HttpServerRequest() {
            @Override
            public String getHttpHeaderValue(String s) {
                return request.getHeader(s);
            }

            @Override
            public URI getUri() {
                return URI.create(request.getRequestURI());
            }

            @Override
            public String getHttpMethod() {
                return request.getMethod();
            }
        };
        ServerRequestAdapter adapter = new HttpServerRequestAdapter(serverRequest, new SpanNameProvider() {
            @Override
            public String spanName(HttpRequest httpRequest) {
                return httpRequest.getUri().getPath();
            }
        });
        brave.serverRequestInterceptor().handle(adapter);
        //16进制字符串
        MDC.put("traceId",Long.toHexString(brave.serverSpanThreadBinder().getCurrentServerSpan().getSpan().getTrace_id()));
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, final HttpServletResponse response, Object handler, Exception ex) throws Exception {
        ServerResponseInterceptor serverResponseInterceptor = this.brave.serverResponseInterceptor();
        ServerResponseAdapter adapter = new HttpServerResponseAdapter(new HttpResponse() {
            @Override
            public int getHttpStatusCode() {
                return response.getStatus();
            }
        });
        serverResponseInterceptor.handle(adapter);
        MDC.remove("traceId");
    }

    public Brave getBrave() {
        return brave;
    }

    public void setBrave(Brave brave) {
        this.brave = brave;
    }

}
