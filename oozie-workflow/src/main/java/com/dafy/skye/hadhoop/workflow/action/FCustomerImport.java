package com.dafy.skye.hadhoop.workflow.action;

import com.dafy.skye.component.*;
import com.dafy.skye.hadhoop.workflow.basic.RegionHelper;
import com.dafy.skye.mapper.FCustomerMapperStatement;
import com.dafy.skye.model.Result;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.io.DataOutputStream;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by quanchengyun on 2017/10/30.
 */
public class FCustomerImport extends BasicOozieAction {

    private static Logger log = LogBuilder.buildLogger(RegionImport.class);

    //private RegionHelper regionHelper = null;
    //清洗后数据保存的hdfs中间文件名
    private static String work_fileName = "customer";

    @Override
    protected void postAction() throws Exception{
        super.postAction();
        String date = DateUtil.getDateformat();
        hiveConnector.execute(hiveconn, FCustomerMapperStatement.drop_backup_table.replaceAll("\\{date\\}",date));
        hiveConnector.execute(hiveconn, FCustomerMapperStatement.backup.replaceAll("\\{date\\}",date));
    }
    @Override
    public Result _doAction(Map configs) {
        try{
            ResultSet rs  = mysqlConnector.query(mysqlconn, FCustomerMapperStatement.get_customers_info,null);
            processOriginalData(rs);
            //
            hiveConnector.execute(hiveconn, FCustomerMapperStatement.drop_table);
            hiveConnector.execute(hiveconn, FCustomerMapperStatement.create_table);
            //then laod the data in local file system
            hiveConnector.insertOrUpdate(hiveconn,FCustomerMapperStatement.load_data, work_dir+"/"+work_fileName);
        }catch(Exception e){
            log.error("导入出错：",e);
        }
        return null;
    }

    @Override
    String getWork_fileName() {
        return work_fileName;
    }

    /**
     * 对原始数据进行处理
     * 1.对region_id 处理
     * @param rs
     */
    private void processOriginalData(ResultSet rs) throws  Exception{
        DataOutputStream fs = getHDFSWriter();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        RegionHelper regionHelper = getRegionHelper();
        try{
            while(rs.next()){
                List<String> values= new ArrayList<>();
                String id = String.valueOf(rs.getLong(1));
                values.add(id);
                values.add(rs.getString(2));
                values.add(rs.getString(3));
                values.add(rs.getString(4));
                values.add(String.valueOf(rs.getInt(5)));
                //4个归属地空白
                values.add(null);
                values.add(null);
                values.add(null);
                values.add(null);
                String live_area = rs.getString(6);
                //居住归属地
                if(StringUtils.isNotBlank(live_area)){
                    String city = StringUtils.split(live_area)[1];
                    String regionId = regionHelper.getRegionId(city);
                    values.add(regionId);
                }else{
                    values.add(null);
                }
                String com = rs.getString(7);
                values.add(com);//company
                String company_area = rs.getString(8);//公司归属地
                if(StringUtils.isNotBlank(company_area)){
                    String[] areasplit = StringUtils.split(company_area);
                    String city = areasplit.length>1?areasplit[1]:areasplit[0];
                    String regionId = regionHelper.getRegionId(city);
                    values.add(regionId);
                }else{
                    values.add(null);
                }
                //性别，出生日期，年龄
                String id_no = rs.getString(2);
                if(StringUtils.isNotBlank(id_no)){
                    String gender = IdParser.getGenderByIdCard(id_no);
                    //M 0 , F 1
                    int genderInt = gender.equals("M")?0:1;
                    values.add(String.valueOf(genderInt));
                    String birthDate = IdParser.getBirthByIdCard(id_no);
                    values.add(birthDate);
                    int age = IdParser.getAgeByIdCard(id_no);
                    values.add(String.valueOf(age));
                }else{
                    values.add(null);
                    values.add(null);
                    values.add(null);
                }
                //bank_mobile
                values.add(rs.getString(9));
                //bank_card_no
                values.add(rs.getString(10));
                //bank_code
                values.add(rs.getString(11));
                //register_channel
                values.add(rs.getString(12));
                //invite_id
                values.add(String.valueOf(rs.getLong(13)));
                //recommender_id
                values.add(String.valueOf(rs.getLong(14)));
                //maintenance_id
                values.add(String.valueOf(rs.getLong(15)));
                //interviewer_id
                values.add(String.valueOf(rs.getLong(16)));
                //
                Date create_time = rs.getDate(17);
                Date update_time = rs.getDate(18);
                values.add(sdf.format(create_time));
                values.add(sdf.format(update_time));
                /**
                 *   "    quota_time TIMESTAMP," +
                 "    quota INT, "+
                 "    remain_quota INT, "+
                 "    credit_status TINYINT, "+
                 "    submit_time TIMESTAMP "+
                 */
                values.add(rs.getDate("quota_time")==null?null:sdf.format(rs.getDate("quota_time")));
                values.add(String.valueOf(rs.getInt("quota")));
                values.add(String.valueOf(rs.getInt("credit_status")));
                values.add(rs.getDate("submit_time")==null?null:sdf.format(rs.getDate("submit_time")));
                values.add(String.valueOf(rs.getInt("store_id")));
                FileHelper.writeLine(fs,"\001",values.toArray(new String[0]));
            }
            fs.flush();
        }finally {
            if(fs!=null){
                fs.close();
            }
            rs.close();
        }
    }

}
