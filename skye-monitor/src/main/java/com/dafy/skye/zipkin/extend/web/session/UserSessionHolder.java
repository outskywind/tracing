package com.dafy.skye.zipkin.extend.web.session;

import com.dafy.skye.zipkin.extend.dto.UserInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by quanchengyun on 2018/4/13.
 */
public class UserSessionHolder {
        //
        private static ThreadLocal<UserInfo>  user = new ThreadLocal<>();
        //threadLocal 有问题，不知是否undertow 底层使用了自定义的线程池
        private static Map<String,Set<String>> userService = new HashMap<>();
        public static UserInfo getUser(){
                UserInfo userInfo = user.get();
                return userInfo;
        }

        public static void setUser(UserInfo userinfo){
                user.set(userinfo);
        }
}
