package com.dafy.skye.context;

import org.springframework.util.StringUtils;

/**
 * order 升序
 *
 * Created by quanchengyun on 2018/10/11.
 */
public class JDBCEnvironmentInterceptor implements EnvironmentInterceptor {

    public static final String appendURL="&statementInterceptors=brave.mysql.TracingStatementInterceptor&zipkinServiceName=";

    String appName;

    int order=0;

    @Override
    public boolean matches(String key, String originV) {
        return StringUtils.hasText(originV)&&originV.startsWith("jdbc:mysql:");
    }

    @Override
    public <T> T replace(T originV) {
        StringBuilder sb = new StringBuilder((String)originV);
        sb.append(appendURL).append(appName);
        return (T)sb.toString();
    }

    @Override
    public int getOrder() {
        return order;
    }


    public void setOrder(int order) {
        this.order=order;
    }


    public void setAppName(String appName) {
        this.appName = appName;
    }
}
