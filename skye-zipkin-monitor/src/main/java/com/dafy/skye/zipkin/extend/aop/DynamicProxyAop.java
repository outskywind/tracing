package com.dafy.skye.zipkin.extend.aop;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.cglib.proxy.Enhancer;

/**
 * Created by quanchengyun on 2018/4/27.
 */

/**
 * 使用代理类直接实现interceptor ，是将advice与 pointcut 直接结合在一起了
 * 这个代理类就是一个 aspect 了，因此在这个代理类中直接实现advice 但是这个比较抽象
 * 使用method interceptor 不会直接限定想要的连接点ponitcut 因此需要利用反射限定实现ponitcut
 *
 */
public class DynamicProxyAop implements FactoryBean,InitializingBean {

    private Advice advice;
    public void setAdvice(Advice advice) {
        this.advice = advice;
    }
    /**
     * since the isSingleton() is true ,then getObject() while only be called once,
     * later dependency inject will use the cached reference
     * @return
     * @throws Exception
     */
    @Override
    public Object getObject() throws Exception {
        //cglib proxy is better  since we want the class can be proxied as well
        Enhancer enhancer = new Enhancer();
        //
        enhancer.setSuperclass(advice.targetClass);
        enhancer.setCallback(advice);
        return enhancer.create();
        //因为 spring boot 注解时就已经cglib创建了一个增强类，注入的实例就是那个增强类
        //然后再对那个增强类使用，会导致方法名重复的问题，无法对一个增强类再次进行增强
        // 因为这个类上面有@Transactional 注解的方法，因此被spring代理了
    }
    /**
     * Object.getClass() forever returns the runtime Class . other than the declared Class
     * @return
     */
    @Override
    public Class<?> getObjectType() {
        return advice.target.getClass();
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }

}
