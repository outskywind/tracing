package com.dafy.skye.component;


import java.sql.Connection;
import java.util.Map;

/**
 * Created by quanchengyun on 2017/10/26.
 */
public class MysqlConnector extends AbstractConnector {

    private static final String MYSQL_CONFIG = "mysql";

    private MysqlConnector(){}

    private static final MysqlConnector instance = new MysqlConnector();

    public static MysqlConnector getInstance(){
        return instance;
    }

    @Override
    public  Connection getConnection(Map configs){
        Map hiveConf = (Map)configs.get(MYSQL_CONFIG);
        return super.getConnection(hiveConf);
    }

}
