package com.dafy.skye.component;

/**
 * Created by quanchengyun on 2017/10/25.
 */

import org.apache.hadoop.HadoopIllegalArgumentException;
import org.apache.hadoop.ipc.Server;
import org.slf4j.Logger;

import java.sql.*;
import java.util.Map;

/**
 *  https://cwiki.apache.org/confluence/display/Hive/HiveServer2+Clients#HiveServer2Clients-JDBC
 */
public class HiveConnector extends AbstractConnector{

    private static final String HIVE_CONFIG = "hive";

    private HiveConnector(){}

    private static final HiveConnector instance =  new HiveConnector();

    public static HiveConnector getInstance(){
        return instance;
    }

    /**
     * 因为是一次性跑的action所以无需缓存
     * @param configs
     * @return
     */
    @Override
    public  Connection getConnection(Map configs){
        Map hiveConf = (Map)configs.get(HIVE_CONFIG);
        return super.getConnection(hiveConf);
    }


}
