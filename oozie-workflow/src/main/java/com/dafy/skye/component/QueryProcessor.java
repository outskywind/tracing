package com.dafy.skye.component;

import java.sql.ResultSet;

/**
 * Created by quanchengyun on 2017/11/5.
 */
public interface QueryProcessor {

    ResultSet query() throws Exception;
}
