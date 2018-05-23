package com.dafy.skye.zipkin.extend.web.websocket;

import org.springframework.web.socket.server.standard.SpringConfigurator;

import javax.websocket.server.ServerEndpoint;

/**
 * Created by quanchengyun on 2018/5/18.
 */
@ServerEndpoint(value="/ws/log/",configurator = SpringConfigurator.class)
public class LogSocket {

}
