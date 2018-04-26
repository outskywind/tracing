package com.dafy.skye.zipkin.extend.web.controller;

import com.dafy.skye.zipkin.extend.dto.Response;
import com.dafy.skye.zipkin.extend.dto.Rule;
import com.dafy.skye.zipkin.extend.dto.UserInfo;
import com.dafy.skye.zipkin.extend.web.HttpServletRequestHelper;
import com.dafy.skye.zipkin.extend.service.RuleService;
import com.dafy.skye.zipkin.extend.web.session.UserSessionHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import javax.servlet.http.HttpServletRequest;
import java.util.List;


/**
 * Created by quanchengyun on 2018/4/12.
 */
@Controller
@RequestMapping(value="/monitor/rule",method = RequestMethod.POST)
public class RuleController extends BaseSessionController{

    @Autowired
    RuleService ruleService;

    @RequestMapping("/add")
    @ResponseBody
    public Response addRule(@RequestBody List<Rule> rules){
        String code = ruleService.add(rules);
        return new Response(code,null);
    }

    @RequestMapping("/list")
    @ResponseBody
    public Response listRule(){
        UserInfo user = getUser();
        List<Rule> rules = ruleService.list(user.getFavServices());
        return new Response("0",rules);
    }


    @RequestMapping("/modify")
    @ResponseBody
    public Response modifyRule(@RequestBody  List<Rule> rules){
        String code = ruleService.modify(rules);
        return new Response(code,null);
    }


    @RequestMapping("/delete")
    @ResponseBody
    public Response deleteRule(@RequestBody  List<Rule> rules){
        String code = ruleService.delete(rules);
        return new Response(code,null);
    }






}
