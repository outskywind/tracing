package com.dafy.skye.zipkin.extend.dto;

import java.util.*;

/**
 * Created by quanchengyun on 2018/4/13.
 */
public class UserInfo {

    private String name;

    private String email;

    private Set<String> favServices = new HashSet<>();

    private Map<String,List<Rule>> ruleMap = new HashMap<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Set<String> getFavServices() {
        return favServices;
    }

    public void setFavServices(Set<String> favServices) {
        this.favServices = favServices;
    }

    public Map<String, List<Rule>> getRuleMap() {
        return ruleMap;
    }

    public void setRuleMap(Map<String, List<Rule>> ruleMap) {
        this.ruleMap = ruleMap;
    }
}
