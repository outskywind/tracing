package com.dafy.skye.brave.servlet;

import brave.Tracer;
import brave.Tracing;
import brave.servlet.TracingFilter;
import com.dafy.skye.brave.ConstantsBrave;
import org.slf4j.MDC;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by quanchengyun on 2018/8/28.
 */
public class ServletTraceFilter implements Filter {

    private Tracer tracer;

    private Filter tracingFilter ;

    public void setTracing(Tracing tracing) {
        this.tracer = tracing.tracer();
        this.tracingFilter = TracingFilter.create(tracing);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,final FilterChain chain) throws IOException, ServletException {
        if(tracingFilter!=null) {
            //modify chain
            tracingFilter.doFilter(request,response,new FilterChain(){
                @Override
                public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
                    if (tracer!=null) {
                        //Span span = tracer.toSpan(tracer.currentSpan().context().toBuilder().traceId(IDGenerator.getId()).build());
                        //使用这个span为当前的span ，无法区分这个traceId是否为自定义的ID，不能这么做，只能自定义Tracing
                        //tracer.withSpanInScope(span);
                        MDC.put(ConstantsBrave.MDC_TRACE_ID_KEY, Long.toHexString(tracer.currentSpan().context().traceId()));
                    }
                    try{
                        chain.doFilter(request,response);
                        after((HttpServletResponse)response,null);
                    }catch (Exception e){
                        after((HttpServletResponse)response,e);
                        throw e;
                    }
                }
            });
        }
    }

    public void after( final HttpServletResponse response,final Throwable exception){
        if(tracer==null){
            return;
        }
        try{
            if(exception==null && response.getStatus()<400){
                tracer.currentSpanCustomizer().tag("status",ConstantsBrave.SUCCESS);
            }
        }catch (Throwable e){

        }finally {
            MDC.remove(ConstantsBrave.MDC_TRACE_ID_KEY);
        }
    }

    @Override
    public void destroy() {

    }
}
