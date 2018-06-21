package com.dafy.skye.zipkin.extend.service;

import com.dafy.skye.zipkin.extend.dto.MonitorMetric;
import com.dafy.skye.zipkin.extend.dto.Rule;
import com.dafy.skye.zipkin.extend.dto.Threshold;
import com.dafy.skye.zipkin.extend.dto.Trace;
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
     * @param trace
     * @param rules 这里已经是整合之后的规则列表，只需直接判断即可
     */
    public void decideStat(Trace trace, Rule[] rules){
        //Stat stat = Stat.green;
        for(Rule rule:rules){
            Stat stat = trace.getStat().get(rule.getDimension());
            Stat decided = doDecide(trace,rule);
            if(stat==null || decided.value()>stat.value()){
                stat = decided;
                trace.getStat().put(rule.getDimension(),stat);
            }
        }
        if(!trace.isSuccess()){
            trace.getStat().put(Demension.SUCCESS_RATE.value(),Stat.red);
        }
    }

    /**
     * 不使用condition字段自己判断
     * @param trace
     * @param rule
     */
    private Stat doDecide(Trace trace, Rule rule){
        if(Demension.LATENCY.value().equals(rule.getDimension())){
            Threshold threshold  = rule.getThreshold();
            if(trace.getLatency() >= threshold.getRed()){
                return Stat.red;
            }
            else if(trace.getLatency() >= threshold.getYellow()){
                return Stat.yellow;
            }
        }
        return Stat.green;
    }



    /**
     * 判断监控维度的状态
     * @param metric
     * @param rules 这里已经是整合之后的规则列表，只需直接判断即可
     */
    public void decideStat(MonitorMetric metric, Rule[] rules){
        //Stat stat = Stat.green;
        for(Rule rule:rules){
            Stat stat = metric.getStat().get(rule.getDimension());
            Stat decided = doDecide(metric,rule);
            if(stat==null || decided.value()>stat.value()){
                stat = decided;
                metric.getStat().put(rule.getDimension(),stat);
            }
        }
        //metric.setStat(stat);
    }


    /**
     * 不使用condition字段自己判断
     * @param metric
     * @param rule
     */
    private Stat doDecide(MonitorMetric metric, Rule rule){
        if(Demension.LATENCY.value().equals(rule.getDimension())){
            Threshold threshold  = rule.getThreshold();
            if(metric.getLatency() >= threshold.getRed()){
                return Stat.red;
            }
            else if(metric.getLatency() >= threshold.getYellow()){
                return Stat.yellow;
            }
        }
        else if(Demension.SUCCESS_RATE.value().equals(rule.getDimension())){
            Threshold threshold  = rule.getThreshold();
            if(metric.getSuccessPercent() <= threshold.getRed()){
                return Stat.red;
            }
            else if(metric.getSuccessPercent() <= threshold.getYellow()){
                return Stat.yellow;
            }
        }
        return Stat.green;
    }
}
