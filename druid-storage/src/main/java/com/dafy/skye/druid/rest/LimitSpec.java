package com.dafy.skye.druid.rest;

import java.util.List;

/**
 * Created by quanchengyun on 2018/5/11.
 */
public class LimitSpec {

    private String type="default";

    private int limit;

    private List<String> columns;

    public String getType() {
        return type;
    }

    public int getLimit() {
        return limit;
    }

    public List<String> getColumns() {
        return columns;
    }
}
