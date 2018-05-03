package com.dafy.skye.zipkin.extend.web.controller;

import com.dafy.skye.zipkin.extend.dto.Response;
import com.dafy.skye.zipkin.extend.dto.Rule;
import com.dafy.skye.zipkin.extend.dto.UserInfo;
import com.dafy.skye.zipkin.extend.enums.ResponseCode;
import com.dafy.skye.zipkin.extend.service.RuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by quanchengyun on 2018/4/12.
 */
@Controller
@RequestMapping(value="/monitor/rule",method = RequestMethod.POST)
public class RuleController extends BaseSessionController{

    @Autowired
    @Qualifier("ruleServiceAop")
    RuleService ruleService;

    @RequestMapping("/add")
    @ResponseBody
    public Response addRule(@RequestBody List<Rule> rules){
        ResponseCode code = ruleService.add(rules);
        return new Response(code.value(),null);
    }

    @RequestMapping("/list")
    @ResponseBody
    public Response listRule(){
        UserInfo user = getUser();
        List<Rule> rules = ruleService.list(user.getFavServices());
        Map<String,List<Rule>> result = new HashMap<>();
        result.put("default",new ArrayList<Rule>());
        result.put("specific",new ArrayList<Rule>());
        for(Rule rule:rules){
            List<Rule> type = result.get(rule.getType());
            type.add(rule);
        }
        return new Response("0",result);
    }


    @RequestMapping("/modify")
    @ResponseBody
    public Response modifyRule(@RequestBody  List<Rule> rules){
        ResponseCode code = ruleService.modify(rules);
        return new Response(code.value(),null);
    }


    @RequestMapping("/delete")
    @ResponseBody
    public Response deleteRule(@RequestBody  List<Integer> rules){
        ResponseCode code = ruleService.delete(rules);
        return new Response(code.value(),null);
    }



}
