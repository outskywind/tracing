package com.dafy.skye.brave.spring.mvc;

import com.github.kristofa.brave.Brave;
import com.github.kristofa.brave.ServerResponseInterceptor;
import com.github.kristofa.brave.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URI;

/**
 * Created by Caedmon on 2017/6/27.
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
        brave.serverRequestInterceptor().handle(new HttpServerRequestAdapter(serverRequest, new SpanNameProvider() {
            @Override
            public String spanName(HttpRequest httpRequest) {
                return httpRequest.getUri().getPath();
            }
        }));
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, final HttpServletResponse response, Object handler, Exception ex) throws Exception {
        ServerResponseInterceptor serverResponseInterceptor = this.brave.serverResponseInterceptor();
        serverResponseInterceptor.handle(new HttpServerResponseAdapter(new HttpResponse() {
            @Override
            public int getHttpStatusCode() {
                return response.getStatus();
            }
        }));
    }

    public Brave getBrave() {
        return brave;
    }

    public void setBrave(Brave brave) {
        this.brave = brave;
    }

}
