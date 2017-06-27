package com.dafy.skye.brave.spring.mvc;

import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
 * Created by Caedmon on 2017/6/27.
 */
public class BraveTracingInterceptor extends HandlerInterceptorAdapter{
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        return super.preHandle(request, response, handler);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        super.afterCompletion(request, response, handler, ex);
    }
}
