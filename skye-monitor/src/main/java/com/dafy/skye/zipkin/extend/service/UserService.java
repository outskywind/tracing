package com.dafy.skye.zipkin.extend.service;

import com.dafy.skye.zipkin.extend.enums.ResponseCode;
import com.dafy.skye.zipkin.extend.mapper.ServiceMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Set;

/**
 * Created by quanchengyun on 2018/4/11.
 */
@Service
public class UserService {

    private static Logger log  = LoggerFactory.getLogger(UserService.class);

    @Autowired
    ServiceMapper serviceMapper;

    public ResponseCode followServices(String user, Set<String> set) {
        try{
            serviceMapper.addUserFollow(user,set);
            return ResponseCode.SUCCESS;
        }catch(Throwable e){
            log.error("关注服务 error.",e);
            return ResponseCode.SYSTEM_ERROR;
        }
    }

    public ResponseCode unfollowServices(String user, Set<String> set) {
        try{
            serviceMapper.deleteUserFollow(user,set);
            return ResponseCode.SUCCESS;
        }catch(Throwable e){
            log.error("取消关注服务 error.",e);
            return ResponseCode.SYSTEM_ERROR;
        }
    }

    public List<String> listFollowServices(String user) {
        try{
            List<String> follows = serviceMapper.listFollow(user);
            return follows;
        }catch(Exception e){
            log.error("获取关注服务 error.",e);
        }
        return null;
    }


}
