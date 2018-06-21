package com.dafy.skye.zipkin.extend.config;

import com.dafy.skye.zipkin.extend.aop.DynamicProxyAop;
import com.dafy.skye.zipkin.extend.aop.RuleCacheRefreshAdvice;
import com.dafy.skye.zipkin.extend.service.RuleService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by quanchengyun on 2018/4/27.
 */
@Configuration
public class AopConfig {

    /**
     * 如何在此优雅的定义ponitcut,而且不要代码耦合原目标类
     * 需要保持原目标类的clean
     * @return
     */
    @Bean("ruleServiceAop")
    public DynamicProxyAop factoryRuleService(){
        DynamicProxyAop aop = new DynamicProxyAop();
        aop.setAdvice(cacheRefreshAdvice());
        return aop;
    }

    @Bean
    public RuleCacheRefreshAdvice<RuleService> cacheRefreshAdvice(){
        RuleCacheRefreshAdvice<RuleService> refreshAdvice = new RuleCacheRefreshAdvice<>();
        refreshAdvice.setPointCut(new String[]{"add","modify","delete"});
        refreshAdvice.setTarget(ruleService(),RuleService.class);
        return refreshAdvice;
    }

    @Bean("ruleService")
    public RuleService ruleService(){
        return new RuleService();
    }


}
