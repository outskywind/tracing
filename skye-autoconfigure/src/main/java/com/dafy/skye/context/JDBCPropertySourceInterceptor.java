package com.dafy.skye.context;

import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;


/**
 * order 升序
 *
 * Created by quanchengyun on 2018/10/11.
 */
public class JDBCPropertySourceInterceptor implements PropertySourceInterceptor ,EnvironmentAware{

    public static final String APPNAME_KEY = "appName";

    public static final String appendURL="&statementInterceptors=brave.mysql.TracingStatementInterceptor&zipkinServiceName=";

    String appName;

    int order=0;

    public JDBCPropertySourceInterceptor(){
    }

    public JDBCPropertySourceInterceptor(String appName){
        this.appName = appName;
    }

    @Override
    public <T> boolean  matches(String key, T originV) {
        return originV instanceof String && StringUtils.hasText((String)originV)&&((String)originV).startsWith("jdbc:mysql:");
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

    @Override
    public void setEnvironment(Environment environment) {
        this.appName = environment.getProperty(APPNAME_KEY);
    }

    public boolean  isOk(){
        return StringUtils.hasText(this.appName);
    }
}
