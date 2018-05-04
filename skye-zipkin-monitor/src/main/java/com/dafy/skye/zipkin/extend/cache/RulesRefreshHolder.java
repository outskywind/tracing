package com.dafy.skye.zipkin.extend.cache;


import com.dafy.skye.zipkin.extend.dto.Rule;
import com.dafy.skye.zipkin.extend.enums.RuleKey;
import com.dafy.skye.zipkin.extend.service.RuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by quanchengyun on 2018/5/3.
 */
@Component
public class RulesRefreshHolder {

    @Autowired
    @Qualifier("ruleService")
    private RuleService ruleService;

    private Cache cache  = new Cache();

    private volatile AtomicBoolean needReload = new AtomicBoolean(true);

    //数组的性能高于list
    public Rule[] getRules(String service,String spanName){
        if(needReload.get()){
            List<Rule> allRules = this.ruleService.listAll();
            reOrganize(allRules);
            needReload.compareAndSet(true,false);
        }
        //先获取特殊规则集
        List<Rule> serviceR = this.cache.get(service);
        List<Rule> interfaceR = this.cache.get(new StringBuilder(service).append(".").append(spanName).toString());
        List<Rule> defaultR = this.cache.get(RuleKey.DEFAULT);
        //优先级整合，同一个维度下优先级 d<s<i
        Map<String,Rule> merged = new HashMap<>();
        if(!CollectionUtils.isEmpty(defaultR)){
            for(Rule r: defaultR){
                merged.put(r.getDimension(),r);
            }
        }
        if(!CollectionUtils.isEmpty(serviceR)){
            for(Rule r: serviceR){
                merged.put(r.getDimension(),r);
            }
        }
        if(!CollectionUtils.isEmpty(interfaceR)){
            for(Rule r: interfaceR){
                merged.put(r.getDimension(),r);
            }
        }
        return merged.values().toArray(new Rule[0]);
    }

    public Rule[] getRules(String service){
        return getRules(service,null);
    }

    public void clear(){
        needReload.compareAndSet(false,true);
        this.cache.clear();
    }


    /**
     * 重新组织规则 按照 key ->List<Rule>
     *    key => default  , service , service.span ,
     * @param rules
     */
    public void reOrganize(List<Rule> rules){
        for(Rule rule :rules){
            String key = rule.key();
            List<Rule> values = this.cache.get(key);
            if (values==null) {
                values=new ArrayList<>();
                this.cache.add(key,values);
            }
            values.add(rule);
        }
    }

}
