package com.dafy.skye.zipkin.extend.cache;


import com.dafy.skye.zipkin.extend.dto.Rule;
import com.dafy.skye.zipkin.extend.dto.UserInfo;
import com.dafy.skye.zipkin.extend.service.RuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by quanchengyun on 2018/5/3.
 */
@Component
public class RulesRefreshHolder {

    @Autowired
    @Qualifier("ruleService")
    private RuleService ruleService;

    private Cache cache  = new Cache();

    private static final String  KEY="rules";

    public List<Rule> getRules(){
        List<Rule> result = this.cache.get(KEY);
        if(result==null){
            result = this.ruleService.listAll();
            this.cache.add(KEY,result,3600);
        }
        return result;
    }

    public void remove(){
        this.cache.remove(KEY);
    }

}
