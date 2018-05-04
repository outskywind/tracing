package com.dafy.skye.zipkin.extend.service;

import com.dafy.skye.zipkin.extend.dto.MonitorMetric;
import com.dafy.skye.zipkin.extend.dto.Rule;
import com.dafy.skye.zipkin.extend.dto.Threshold;
import com.dafy.skye.zipkin.extend.enums.Demension;
import com.dafy.skye.zipkin.extend.enums.ResponseCode;
import com.dafy.skye.zipkin.extend.enums.Stat;
import com.dafy.skye.zipkin.extend.mapper.RuleMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * Created by quanchengyun on 2018/4/12.
 */
public class RuleService{

    private static Logger log = LoggerFactory.getLogger(RuleService.class);

    @Autowired
    ObjectMapper json;

    @Autowired
    RuleMapper ruleMapper;

    public ResponseCode add(List<Rule> rules){
        try{
            for(Rule rule:rules){
                rule.setThresholdStr(json.writeValueAsString(rule.getThreshold()));
            }
            ruleMapper.add(rules);
            return ResponseCode.SUCCESS;
        }catch(JsonProcessingException e){
            log.error("json异常:",e);
        }catch (Exception e){
            if(e instanceof DuplicateKeyException){
                log.error("规则冲突.",e);
                return ResponseCode.RULE_DUPLICATE;
            }
        }
        return ResponseCode.SYSTEM_ERROR;
    }


    public List<Rule> list(Set<String> favServices) {
        try{
            List<Rule>  result = ruleMapper.list(favServices);
            for(Rule rule:result){
                rule.setThreshold(json.readValue(rule.getThresholdStr(), Threshold.class));
            }
            return result;
        }catch (Exception e){
            log.error("查询出错:",e);
        }
        return null;
    }

    public List<Rule> listAll() {
        try{
            List<Rule>  result = ruleMapper.listAll();
            for(Rule rule:result){
                rule.setThreshold(json.readValue(rule.getThresholdStr(), Threshold.class));
            }
            return result;
        }catch (Exception e){
            log.error("查询出错:",e);
        }
        return null;
    }



    @Transactional(rollbackFor = Exception.class)
    public ResponseCode modify(List<Rule> rules) {
        try{
            for(Rule rule: rules){
                rule.setThresholdStr(json.writeValueAsString(rule.getThreshold()));
                ruleMapper.modify(rule);
            }
        }catch(Exception e){
            log.error("修改出错: ",e);
            return ResponseCode.SYSTEM_ERROR;
        }
        return ResponseCode.SUCCESS;
    }

    @Transactional(rollbackFor = Exception.class)
    public ResponseCode delete(List<Integer> rules) {
        try{
            ruleMapper.delete(rules);
        }catch(Exception e){
            log.error("删除出错: ",e);
            return ResponseCode.SYSTEM_ERROR;
        }
        return ResponseCode.SUCCESS;
    }


    /**
     * 判断监控维度的状态
     * @param metric
     * @return
     */
    public void decideStat(MonitorMetric metric, List<Rule> rules){
        //key service.interface ->rule
        //    default ->rule
        for(Rule rule:rules){
            if("default".equals(rule.getType())){
                doDecide(metric,rule);
            }
            else if(metric.name.equals(rule.getService()) || metric.name.equals(rule.getSpanName())){
                doDecide(metric,rule);
                break;
            }
        }
    }


    private void doDecide(MonitorMetric metric, Rule rule){
        if(Demension.LATENCY.value().equals(rule.getDimension())){
            switch (rule.getCondition()){
                case ">=" :  {
                    Threshold threshold  = rule.getThreshold();
                    if(metric.getLatency() >= threshold.getRed()){
                        metric.setStat(Stat.red);
                    }
                    else if(metric.getLatency() >= threshold.getYellow()){
                        metric.setStat(Stat.yellow);
                    }
                    break;
                }
                case "<=" : {
                    //nothing to do ..
                    break;
                }
            }
        }
        else if(Demension.SUCCESS_RATE.value().equals(rule.getDimension())){
            switch (rule.getCondition()){
                case ">=" :  {
                    Threshold threshold  = rule.getThreshold();
                    if(metric.getLatency() >= threshold.getRed()){
                        metric.setStat(Stat.red);
                    }
                    else if(metric.getLatency() >= threshold.getYellow()){
                        metric.setStat(Stat.yellow);
                    }
                    break;
                }
                case "<=" : {
                    //nothing to do ..
                    break;
                }
            }
        }
    }
}
