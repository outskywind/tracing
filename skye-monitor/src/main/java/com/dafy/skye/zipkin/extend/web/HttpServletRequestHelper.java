package com.dafy.skye.zipkin.extend.web;

import com.dafy.skye.zipkin.extend.dto.UserInfo;
import com.dafy.skye.zipkin.extend.web.filter.FilterCallback;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.BASE64Decoder;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Created by quanchengyun on 2018/4/13.
 */
public class HttpServletRequestHelper {

    private static Logger log = LoggerFactory.getLogger(HttpServletRequestHelper.class);

    private final static String cookieName="zeus-auth";

    static ObjectMapper json = InstanceHolder.json;

    static class InstanceHolder {
        public static ObjectMapper json = initjson();
        static ObjectMapper initjson(){
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false);
            return mapper;
        }
    }

    public static UserInfo getUserfromSessionCookie(HttpServletRequest request,FilterCallback<UserInfo> callback){
        HttpSession session = request.getSession();
        UserInfo u=null;
        if(session!=null){
            u = (UserInfo)session.getAttribute("user");
        }
        //no session
        try{
            if(u==null){
                Cookie[] cookie = request.getCookies();
                if(cookie == null) return null;
                for(Cookie c : cookie){
                    if(cookieName.equals(c.getName())){
                        String  base64V = c.getValue();
                        log.info("cookie value= {}",base64V);
                        BASE64Decoder decoder = new BASE64Decoder();
                        byte[] bytes = decoder.decodeBuffer(base64V);
                        String jsonstr = new String(bytes,"utf-8");
                        u = json.readValue(jsonstr, UserInfo.class);
                        //do something here
                        callback.callback(u);
                        request.getSession(true).setAttribute("user",u);
                        break;
                    }
                }
            }
        }catch (Exception e){
            log.error("获取用户异常:",e);
        }
        return u;
    }

    public static void setUserSession(HttpServletRequest request, UserInfo userInfo){
        HttpSession session = request.getSession();
        if(session!=null){
            session.setAttribute("user",userInfo);
        }
    }



}
