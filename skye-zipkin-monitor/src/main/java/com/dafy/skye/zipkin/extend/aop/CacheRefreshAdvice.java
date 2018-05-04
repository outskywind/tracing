package com.dafy.skye.zipkin.extend.aop;

import com.dafy.skye.zipkin.extend.cache.RulesRefreshHolder;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by quanchengyun on 2018/5/3.
 */
public class CacheRefreshAdvice<T> extends Advice<T> {

    @Autowired
    RulesRefreshHolder holder;

    @Override
    public void before() {

    }

    @Override
    public void setTarget(T target,Class<T> targetClass) {
        super.setTarget(target,targetClass);
    }

    @Override
    public void after(Object result) {
        holder.remove();
    }
}
