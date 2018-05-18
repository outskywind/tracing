package com.dafy.skye.zipkin.extend.dto;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by quanchengyun on 2018/5/17.
 */
public class ServiceInfo {

    private Set<String> interfaces = new HashSet<>();

    private Set<String> hosts = new HashSet<>();


    public void addInterface(String _interface){
        interfaces.add(_interface);
    }

    public void addHost(String host){
        hosts.add(host);
    }

    public Set<String> getInterfaces() {
        return interfaces;
    }

    public void setInterfaces(Set<String> interfaces) {
        this.interfaces = interfaces;
    }

    public Set<String> getHosts() {
        return hosts;
    }

    public void setHosts(Set<String> hosts) {
        this.hosts = hosts;
    }
}
