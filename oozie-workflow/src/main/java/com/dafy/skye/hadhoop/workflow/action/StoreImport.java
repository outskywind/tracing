package com.dafy.skye.hadhoop.workflow.action;

import com.dafy.skye.component.*;
import com.dafy.skye.hadhoop.workflow.ActionLoader;
import com.dafy.skye.hadhoop.workflow.basic.HiveDataHelper;
import com.dafy.skye.hadhoop.workflow.basic.RegionHelper;
import com.dafy.skye.mapper.RegionMapperStatement;
import com.dafy.skye.mapper.StoreMapperStatement;
import com.dafy.skye.model.Result;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.slf4j.Logger;

import java.io.DataOutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by quanchengyun on 2017/10/26.
 */
/**
 *  //导入门店，关联区域表id , regions表
 */
public class StoreImport extends  BasicOozieAction{

    private static Logger log = LogBuilder.buildLogger(StoreImport.class);

    //private static final Connector mysqlConnector = MysqlConnector.getInstance();
    //private static final Connector hiveConnector = HiveConnector.getInstance();

    //清洗后数据保存的hdfs中间文件名
    private static final String work_fileName = "store";

    private static RegionHelper regionHelper = null;

    @Override
    protected void postAction() throws Exception{
        super.postAction();
        String date = DateUtil.getDateformat();
        hiveConnector.execute(hiveconn, StoreMapperStatement.backup.replaceAll("\\{date\\}",date));
    }
    @Override
    public Result _doAction(Map configs) {

        DataOutputStream fs = null;
        try{
            ResultSet rs  = mysqlConnector.query(mysqlconn, StoreMapperStatement.get_store,null);
            regionHelper = RegionHelper.getInstance(hiveConnector,hiveconn);
            fs = getHDFSWriter();
            if(fs == null){
                return null;
            }
            while(rs.next()){
                long id = rs.getLong(1);
                String name = rs.getString(2);
                //String province = rs.getString(3);
                String city = rs.getString(4);
                //Date upDate = rs.getDate(5);
                //关联区域表，只能一次全部查询出来，map-reduce job 太慢了
                String regionId = regionHelper.getRegionId(city);
                //String dateStr
                FileHelper.writeLine(fs,"\t",String.valueOf(id),name,regionId);
            }
            rs.close();
            fs.flush();
            //导入hive表
            hiveConnector.execute(hiveconn,StoreMapperStatement.drop_table);
            hiveConnector.execute(hiveconn,StoreMapperStatement.create_table);
            hiveConnector.insertOrUpdate(hiveconn,StoreMapperStatement.load_data,work_dir+"/"+work_fileName);
        }catch(Exception e){
            log.error("导入门店失败：{}",e);
        }
        finally {
            try{
                if(fs!=null){
                    fs.close();
                }
            }catch(Exception e){
                log.error("connection close error.",e);
            }
        }
        return null;
    }


    @Override
    String getWork_fileName() {
        return work_fileName;
    }

}
