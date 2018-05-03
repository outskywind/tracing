package com.dafy.skye.zipkin.extend.aop;

import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;


/**
 * Created by quanchengyun on 2018/5/3.
 */
public abstract  class Advice<T> implements MethodInterceptor {

    protected T target;

    protected String[] pointCut;

    protected Class<T> targetClass;

    public void setTarget(T target,Class<T> targetClass) {
        this.target = target;
        this.targetClass= targetClass;
    }

    public void setPointCut(String[] pointCut) {
        this.pointCut = pointCut;
    }

    public abstract void before();
    public abstract void after(Object result);

    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        boolean ismatch = false;
        for(String name: pointCut){
            if(methodProxy.getSignature().getName().equals(name)){
                ismatch = true;
                break;
            }
        }
        if (!ismatch){
            // intercept the aspect here
            return method.invoke(target,objects);
        }
        before();
        Object result =  method.invoke(target,objects);
        after(result);
        return result;
    }

}
