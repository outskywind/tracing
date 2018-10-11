package com.dafy.skye.context;

/**
 * order 小序列
 *
 * Created by quanchengyun on 2018/10/11.
 */
public class JDBCEnvironmentInterceptor implements EnvironmentInterceptor {
    @Override
    public boolean matches(String key, String originV) {
        return false;
    }

    @Override
    public <T> T replace(T originV) {
        return null;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
