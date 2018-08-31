package com.dafy.skye.brave.spring.mvc;

import com.github.kristofa.brave.Brave;
import com.github.kristofa.brave.ServerRequestAdapter;
import com.github.kristofa.brave.ServerResponseAdapter;
import com.github.kristofa.brave.ServerResponseInterceptor;
import com.github.kristofa.brave.http.*;
import org.slf4j.MDC;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;

/**
 * Created by quanchengyun on 2018/8/28.
 */
public class TraceFilter implements Filter {

    private Brave brave;

    public void setBrave(Brave brave){
        this.brave = brave;
    }


    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        before((HttpServletRequest)request,(HttpServletResponse)response);
        try{
            chain.doFilter(request,response);
            after((HttpServletRequest)request,(HttpServletResponse)response,null);
        }catch (Exception e){
            after((HttpServletRequest)request,(HttpServletResponse)response,e);
            throw e;
        }
    }

    public void before(final HttpServletRequest request, HttpServletResponse response){
        try{
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
            if(brave==null){
                return;
            }
            ServerRequestAdapter adapter = new HttpServerRequestAdapter(serverRequest, new SpanNameProvider() {
                @Override
                public String spanName(HttpRequest httpRequest) {
                    return httpRequest.getUri().getPath();
                }
            });
            brave.serverRequestInterceptor().handle(adapter);
            //16进制字符串
            MDC.put("traceId",Long.toHexString(brave.serverSpanThreadBinder().getCurrentServerSpan().getSpan().getTrace_id()));

        }catch (Exception e){

        }
    }

    public void after(HttpServletRequest request, final HttpServletResponse response,final Throwable exception){
        try{
            if(brave==null){
                return;
            }
            ServerResponseInterceptor serverResponseInterceptor = this.brave.serverResponseInterceptor();
            ServerResponseAdapter adapter = new HttpServerResponseAdapter(new HttpResponse() {
                @Override
                public int getHttpStatusCode() {
                    if(exception!=null){
                        return 500;
                    }
                    return response.getStatus();
                }
            });
            serverResponseInterceptor.handle(adapter);
            //MDC.remove("traceId");
        }catch (Exception e){

        }finally {
            MDC.remove("traceId");
        }
    }



    @Override
    public void destroy() {

    }
}
