package com.dafy.skye.context;

/**
 * Created by quanchengyun on 2018/10/11.
 */
public interface EnvironmentInterceptor {

      boolean matches(String key,String originV);

      <T> T replace(T originV);

      int getOrder();

      void setAppName(String appName);
}
