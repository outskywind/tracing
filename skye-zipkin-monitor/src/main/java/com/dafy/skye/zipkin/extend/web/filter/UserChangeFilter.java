package com.dafy.skye.zipkin.extend.web.filter;

import com.dafy.skye.zipkin.extend.dto.UserInfo;
import com.dafy.skye.zipkin.extend.service.UserService;
import com.dafy.skye.zipkin.extend.web.session.UserSessionHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;

/**
 * 同步更新session里的用户关注列表，其他的接口将直接从session里获取
 */
@Component
public class UserChangeFilter implements Filter {

    @Autowired
    UserService userService;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        chain.doFilter(request,response);
        UserInfo user = UserSessionHolder.getUser();
        List<String> follow = userService.listFollowServices(user.getEmail());
        user.setFavServices(new HashSet<String>(follow));
        UserSessionHolder.setUser(user);
    }

    @Override
    public void destroy() {

    }
}
