package com.dafy.skye.zipkin.extend.web.controller;

import com.dafy.skye.zipkin.extend.dto.Response;
import com.dafy.skye.zipkin.extend.dto.UserInfo;
import com.dafy.skye.zipkin.extend.enums.ResponseCode;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by quanchengyun on 2018/5/2.
 */
@RestController("/user")
public class UserController extends BaseSessionController{

    @RequestMapping("/info")
    public Response<?> userInfo(){
        UserInfo user =getUser();
        Map<String,String> userMap = new HashMap<>();
        userMap.put("name",user.getName());
        return new Response<>(ResponseCode.SUCCESS.value(),userMap);
    }
}
