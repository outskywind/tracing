package com.dafy.skye.component;

import org.slf4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;

/**
 * Created by quanchengyun on 2017/10/26.
 */
public abstract class AbstractConnector implements Connector {

    private  final String CONNECT_URL = "connectUrl";
    private  final String CONNECT_USER = "user";
    private  final String CONNECT_PASSWORD = "password";
    private  final String CONNECT_DRIVER_CLASS = "driver.class";

    private static Logger log = LogBuilder.buildLogger(HiveConnector.class);

    @Override
    public Connection getConnection(Map connetConf) {
        Connection con = null;
        String connectUrl = null;
        String user = null;
        String password = null;
        try {
            //
            Class.forName((String)connetConf.get(CONNECT_DRIVER_CLASS));
            connectUrl = (String)connetConf.get(CONNECT_URL);
            user = (String)connetConf.get(CONNECT_USER);
            password = (String)connetConf.get(CONNECT_PASSWORD);
            con = DriverManager.getConnection(connectUrl, user, password);
        } catch (Exception e) {
            log.error("建立jdbc连接出错：{} {} {}",connectUrl,user,password,e);
        }
        return con;
    }

    @Override
    public  void execute(Connection conn, String statement) throws Exception{
        PreparedStatement ps = conn.prepareStatement(statement);
        ps.execute();
    }


    /**
     * ? is a placeholder to be replaced by actual variable value
     * @param conn
     * @param statement
     * @param params
     * @throws Exception
     */
    @Override
    public ResultSet query(Connection conn, String statement , Object... params) throws Exception{
        PreparedStatement ps = conn.prepareStatement(statement);
        if (params!=null&& params.length>0){
            for(int i=0;i<params.length;i++){
                ps.setObject(i+1,params[i]);
            }
        }
        ResultSet rs = ps.executeQuery();
        //ps.close(); 会导致 Rs 关闭
        return rs;
    }

    @Override
    public  int insertOrUpdate(Connection conn, String statement , Object... params) throws Exception{
        PreparedStatement ps = conn.prepareStatement(statement);
        if (params!=null&& params.length>0){
            for(int i=0;i<params.length;i++){
                ps.setObject(i+1,params[i]);
            }
        }
        int count =  ps.executeUpdate();
        ps.close();
        return count;
    }
}
