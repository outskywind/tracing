package com.dafy.skye.zipkin.extend.service;

import com.dafy.skye.zipkin.extend.dto.Rule;
import com.dafy.skye.zipkin.extend.mapper.RuleMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * Created by quanchengyun on 2018/4/12.
 */
@Service
public class RuleService {

    private static Logger log = LoggerFactory.getLogger(RuleService.class);

    @Autowired
    ObjectMapper json;

    @Autowired
    RuleMapper ruleMapper;

    private static final String SYSTEM_ERROR="系统错误";


    public String add(List<Rule> rules){
        try{
            for(Rule rule:rules){
                rule.setThresholdStr(json.writeValueAsString(rule.getThreshold()));
            }
            ruleMapper.add(rules);
            return "0";
        }catch(JsonProcessingException e){
            log.error("json异常:",e);
        }catch (Exception e){
            if(e instanceof DuplicateKeyException){
                log.error("规则冲突.",e);
                return "规则冲突";
            }
        }
        return SYSTEM_ERROR;
    }


    public List<Rule> list(Set<String> favServices) {
        try{
            return ruleMapper.list(favServices);
        }catch (Exception e){
            log.error("查询出错:",e);
        }
        return null;
    }

    @Transactional(rollbackFor = Exception.class)
    public String modify(List<Rule> rules) {
        try{
            for(Rule rule: rules){
                ruleMapper.modify(rule);
            }
        }catch(Exception e){
            log.error("修改出错: ",e);
            return SYSTEM_ERROR;
        }
        return "0";
    }

    @Transactional(rollbackFor = Exception.class)
    public String delete(List<Rule> rules) {
        try{
            ruleMapper.delete(rules);
        }catch(Exception e){
            log.error("删除出错: ",e);
            return SYSTEM_ERROR;
        }
        return "0";
    }
}
