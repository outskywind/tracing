package com.dafy.skye.zipkin.extend.web.filter;

import com.dafy.skye.zipkin.extend.dto.UserInfo;
import com.dafy.skye.zipkin.extend.service.UserService;
import com.dafy.skye.zipkin.extend.web.HttpServletRequestHelper;
import com.dafy.skye.zipkin.extend.web.session.UserSessionHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.Collections;

/**
 * Created by quanchengyun on 2018/4/13.
 */
@Component
public class UserFilter implements Filter {

    //#{} 这里注入的需要是注册的bean的属性表达式 ${}才是占位符，使用propertysourceholderconfigurar解析注入
    @NotNull
    @Value("${login.url}")
    String loginUrl;

    @Autowired
    UserService userService;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        final UserInfo user = HttpServletRequestHelper.getUserfromSessionCookie((HttpServletRequest) request, new FilterCallback<UserInfo>() {
            @Override
            public void callback(UserInfo data) {
                data.getFavServices().addAll(userService.listFollowServices(data.getEmail()));
            }
        });
        if(user == null){
            //((HttpServletResponse )response).sendRedirect(loginUrl);
            response.getWriter().write("{\"code\":\"no user\",\"result\":null}");
            response.setContentType("application/json");
            response.flushBuffer();
            return;
        }
        UserSessionHolder.setUser(user);
        chain.doFilter(request,response);
    }

    @Override
    public void destroy() {

    }
}
