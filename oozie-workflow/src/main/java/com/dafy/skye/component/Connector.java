package com.dafy.skye.component;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Map;

/**
 * Created by quanchengyun on 2017/10/26.
 */
public interface  Connector {

     Connection getConnection(Map configs);

     void execute(Connection conn, String statement) throws Exception;

     ResultSet query(Connection conn, String statement , Object... params) throws Exception;

     int insertOrUpdate(Connection conn, String statement , Object... params) throws Exception;



}
