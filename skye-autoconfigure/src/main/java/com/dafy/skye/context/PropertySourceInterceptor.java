package com.dafy.skye.context;

/**
 * Created by quanchengyun on 2018/10/11.
 */
public interface PropertySourceInterceptor {

     <T> boolean matches(String key,T originV);

      <T> T replace(T originV);

      int getOrder();

      boolean isOk();
}
