package com.dafy.skye.zipkin.extend.web.session;

import com.dafy.skye.zipkin.extend.dto.UserInfo;

/**
 * Created by quanchengyun on 2018/4/13.
 */
public class UserSessionHolder {

        private static ThreadLocal<UserInfo>  user = new ThreadLocal<>();
        public static UserInfo getUser(){
                return user.get();
        }

        public static void setUser(UserInfo userinfo){
                user.set(userinfo);
        }



}
