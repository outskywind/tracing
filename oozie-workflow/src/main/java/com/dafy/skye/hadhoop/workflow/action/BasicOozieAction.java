package com.dafy.skye.hadhoop.workflow.action;

import com.dafy.skye.component.*;
import com.dafy.skye.hadhoop.workflow.ActionLoader;
import com.dafy.skye.hadhoop.workflow.basic.RegionHelper;
import com.dafy.skye.model.Result;
import org.slf4j.Logger;

import java.io.DataOutputStream;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

/**
 * Created by quanchengyun on 2017/10/31.
 */
public abstract class BasicOozieAction implements OozieAction {

    private static Logger log = LogBuilder.buildLogger(BasicOozieAction.class);

    protected static final Connector mysqlConnector = MysqlConnector.getInstance();
    protected static final Connector hiveConnector = HiveConnector.getInstance();

    protected Connection mysqlconn =null;
    protected Connection hiveconn =null;

    protected String work_dir = null;
    protected String work_fileName = null;

    @Override
    public final Result doAction(Map configs) throws Exception{
        init(configs);
        try{
            preAction();
            Result result =  _doAction(configs);
            postAction();
            return null;
        }finally {
            clear();
        }
    }

    protected void init(Map configs){
        work_dir = (String)configs.get(ActionLoader.CONFIG_HIVE_WORK_DIR);
        mysqlconn = mysqlConnector.getConnection(configs);
        hiveconn = hiveConnector.getConnection(configs);
        work_fileName = getWork_fileName();
    };

    protected void clear(){
        try{
            if (hiveconn!=null){
                hiveconn.close();
            }
            if (mysqlconn!=null){
                mysqlconn.close();
            }
        }catch(Exception e){
            log.error("connection close error.",e);
        }
    }


    protected DataOutputStream getHDFSWriter(){
        return FileHelper.getHDFSWriter(work_dir,work_fileName);
    }

    protected RegionHelper getRegionHelper(){
        return RegionHelper.getInstance(hiveConnector,hiveconn);
    }

    abstract Result _doAction(Map configs);

    abstract String getWork_fileName();

    protected  void preAction() throws Exception{
    };

    /**
     * 在 java 代码中无法指定 hive-site.xml ,因此需要指定各种HDFS路径
     * @throws Exception
     */
    protected  void postAction() throws Exception{
        //创建备份数据库
        String create_database = "create database if not exists sevend{date} location '/user/root/hive/warehouse'".replace("{date}",DateUtil.getDateformat());
        hiveConnector.execute(hiveconn, create_database);
    };

}
