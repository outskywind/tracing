package com.dafy.skye.zipkin.extend.web.filter;

/**
 * Created by quanchengyun on 2018/4/25.
 */
public interface FilterCallback<T> {

     void callback(T data);
}
