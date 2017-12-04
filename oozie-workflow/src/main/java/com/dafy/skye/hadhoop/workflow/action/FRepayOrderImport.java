package com.dafy.skye.hadhoop.workflow.action;

import com.dafy.skye.component.DateUtil;
import com.dafy.skye.component.FileHelper;
import com.dafy.skye.component.LogBuilder;
import com.dafy.skye.mapper.FLoanOrderMapperStatement;
import com.dafy.skye.mapper.FRepayOrderMapperStatement;
import com.dafy.skye.model.Result;
import org.slf4j.Logger;

import java.io.DataOutputStream;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by quanchengyun on 2017/11/9.
 */
public class FRepayOrderImport extends BasicOozieAction{

    private static Logger log = LogBuilder.buildLogger(FRepayOrderImport.class);

    //清洗后数据保存的hdfs中间文件名
    private static String work_fileName = "repayOrder";

    /**
     * 跑完后直接备份数据
     */
    @Override
    protected void postAction() throws Exception{
        super.postAction();
        //TODO 判断避免重复备份
        String date = DateUtil.getDateformat();
        hiveConnector.execute(hiveconn, FRepayOrderMapperStatement.backup.replaceAll("\\{date\\}",date));
    }

    @Override
    Result _doAction(Map configs) {
        try{
            ResultSet rs  = mysqlConnector.query(mysqlconn, FRepayOrderMapperStatement.get_main_data,null);
            processOriginalData(rs);
            //
            hiveConnector.execute(hiveconn, FRepayOrderMapperStatement.drop_table);
            hiveConnector.execute(hiveconn, FRepayOrderMapperStatement.create_table);
            //then load the data in local file system
            hiveConnector.insertOrUpdate(hiveconn,FRepayOrderMapperStatement.load_data, work_dir+"/"+work_fileName);
        }catch(Exception e){
            log.error("导入出错：",e);
        }
        return null;
    }



    private void processOriginalData(ResultSet rs) throws Exception{
        DataOutputStream fs = getHDFSWriter();
        try{
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            while(rs.next()){
                List<String> values= new ArrayList<>();
                values.add(rs.getString("loan_order_no"));
                values.add(rs.getString("repay_order_no"));
                values.add(String.valueOf(rs.getLong("customer_id")));
                values.add(String.valueOf(rs.getInt("principal")));
                values.add(String.valueOf(rs.getInt("interest")));
                values.add(String.valueOf(rs.getInt("fee")));
                values.add(String.valueOf(rs.getInt("damages")));
                values.add(String.valueOf(rs.getInt("status")));
                values.add(String.valueOf(rs.getInt("loan_type")));
                values.add(String.valueOf(rs.getInt("product_type")));
                values.add(String.valueOf(rs.getInt("order_type")));
                values.add(String.valueOf(rs.getInt("initiator")));
                values.add(rs.getString("failure_code"));
                values.add(rs.getString("failure_msg"));
                values.add(sdf.format(rs.getDate("repay_time")));
                values.add(String.valueOf(rs.getInt("repay_days_after_loan")));
                values.add(String.valueOf(rs.getInt("state")));
                values.add(sdf.format(rs.getDate("create_time")));
                values.add(sdf.format(rs.getDate("update_time")));
                FileHelper.writeLine(fs,"\001",values.toArray(new String[0]));
            }
            fs.flush();
        }finally {
            if(rs!=null){
                rs.close();
            }
            fs.close();
        }
    }

    @Override
    String getWork_fileName() {
        return work_fileName;
    }
}
