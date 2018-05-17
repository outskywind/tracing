package com.dafy.skye.zipkin.extend.web.controller;

import com.dafy.skye.zipkin.extend.dto.UserInfo;
import com.dafy.skye.zipkin.extend.web.session.UserSessionHolder;

/**
 * Created by quanchengyun on 2018/4/25.
 */
public class BaseSessionController {

    public UserInfo  getUser(){
        UserInfo userInfo = UserSessionHolder.getUser();
        return userInfo;
    }


}
