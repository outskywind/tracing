package com.dafy.skye.hadhoop.workflow.action;

import com.dafy.skye.component.*;
import com.dafy.skye.mapper.FLoanOrderMapperStatement;
import com.dafy.skye.model.LoanSeq;
import com.dafy.skye.model.Remain;
import com.dafy.skye.model.RepaySum;
import com.dafy.skye.model.Result;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;
import org.slf4j.Logger;

import java.io.DataOutputStream;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Created by quanchengyun on 2017/11/3.
 */
public class FLoanOrderImport extends BasicOozieAction{

    private static Logger log = LogBuilder.buildLogger(FLoanOrderImport.class);

    //清洗后数据保存的hdfs中间文件名
    private static String work_fileName = "loanOrder";

    /**
     * 跑完后直接备份数据
     */
    @Override
    protected void postAction() throws Exception{
        super.postAction();
        //TODO 判断避免重复备份
        String date = DateUtil.getDateformat();
        hiveConnector.execute(hiveconn, FLoanOrderMapperStatement.backup.replaceAll("\\{date\\}",date));
    }

    @Override
    Result _doAction(Map configs) {
        try{
            ResultSet rs  = mysqlConnector.query(mysqlconn, FLoanOrderMapperStatement.get_main_data,null);
            processOriginalData(rs);
            //
            hiveConnector.execute(hiveconn, FLoanOrderMapperStatement.drop_table);
            hiveConnector.execute(hiveconn, FLoanOrderMapperStatement.create_table);
            //then load the data in local file system
            hiveConnector.insertOrUpdate(hiveconn,FLoanOrderMapperStatement.load_data, work_dir+"/"+work_fileName);
        }catch(Exception e){
            log.error("导入出错：",e);
        }
        return null;
    }

    private void processOriginalData(ResultSet rs) throws Exception{

        DB db = DBMaker
                .memoryDB()
                .make();
        //loan_seq 处理
        final BTreeMap<String, LoanSeq> seqBTreeMap = db.treeMap("seq")
                .keySerializer(Serializer.STRING)
                .valueSerializer(Serializer.JAVA)
                .createOrOpen();
        DataOutputStream fs = getHDFSWriter();

        Future  f_seq = JDBCHelper.build().withQuery(new QueryProcessor() {
            @Override
            public ResultSet query() throws Exception{
                return mysqlConnector.query(mysqlconn, FLoanOrderMapperStatement.get_calculate_data_loanSeq,null);
            }
        }).withResultSetProcessor(new ResultSetProcessor() {
            @Override
            public <T> T process(ResultSet rs) throws Exception{
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                while(rs.next()){
                    String loan_order_no = rs.getString(1);
                    Date loan_time = rs.getDate(2);
                    int loan_seq = rs.getInt(3);
                    LoanSeq cur = new LoanSeq();
                    cur.loan_seq = loan_seq;
                    cur.loan_time = sdf.format(loan_time);
                    seqBTreeMap.put(loan_order_no,cur);
                }
                return null;
            }
        }).run();
        //repay 处理
        final BTreeMap<String, RepaySum> repayBTreeMap = db.treeMap("repay")
                .keySerializer(Serializer.STRING)
                .valueSerializer(Serializer.JAVA)
                .createOrOpen();

        Future  f_repay = JDBCHelper.build().withQuery(new QueryProcessor() {
            @Override
            public ResultSet query() throws Exception {
                return  mysqlConnector.query(mysqlconn, FLoanOrderMapperStatement.get_calculate_data_repaySum,null);
            }
        }).withResultSetProcessor(new ResultSetProcessor() {
            @Override
            public <T> T process(ResultSet rs) throws Exception {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                while(rs.next()){
                    String loan_order_no = rs.getString(1);
                    int repay_principal = rs.getInt(2);
                    Date finish_time = rs.getDate(3);
                    int repay_interest = rs.getInt(4);
                    int repay_damages = rs.getInt(5);
                    int repay_fee = rs.getInt(6);
                    RepaySum cur = new RepaySum();
                    cur.repay_principal = repay_principal;
                    cur.finish_time = sdf.format(finish_time);
                    cur.repay_interest = repay_interest;
                    cur.repay_damages = repay_damages;
                    cur.repay_fee = repay_fee;
                    repayBTreeMap.put(loan_order_no,cur);
                }
                return null;
            }
        }).run();

        //overdue 处理
        final BTreeMap<String, Integer> overdueBTreeMap = db.treeMap("overdue")
                .keySerializer(Serializer.STRING)
                .valueSerializer(Serializer.INTEGER)
                .createOrOpen();
        Future  f_overdue = JDBCHelper.build().withQuery(new QueryProcessor() {
            @Override
            public ResultSet query() throws Exception {
                return mysqlConnector.query(mysqlconn, FLoanOrderMapperStatement.getGet_calculate_data_overdue,null);
            }
        }).withResultSetProcessor(new ResultSetProcessor() {
            @Override
            public <T> T process(ResultSet rs) throws Exception {
                while(rs.next()){
                    String loan_order_no = rs.getString(1);
                    int should_damages = rs.getInt(2);
                    overdueBTreeMap.put(loan_order_no,should_damages);
                }
                return null;
            }
        }).run();

        //TODO  应还计算
        //remain 处理
        final BTreeMap<String, Remain> remainBTreeMap = db.treeMap("remain")
                .keySerializer(Serializer.STRING)
                .valueSerializer(Serializer.JAVA)
                .createOrOpen();
        Future  f_remain = JDBCHelper.build().withQuery(new QueryProcessor() {
            @Override
            public ResultSet query() throws Exception {
                return mysqlConnector.query(mysqlconn, FLoanOrderMapperStatement.getGet_calculate_data_remain,null);
            }
        }).withResultSetProcessor(new ResultSetProcessor() {
            @Override
            public <T> T process(ResultSet rs) throws Exception {
                while(rs.next()){
                    Remain remain = new Remain();
                    remain.loan_order_no = rs.getString(1);
                    remain.principal_damages_rate = rs.getDouble(2);
                    remain.remain_interest = rs.getInt(3);
                    remain.remain_damages = rs.getInt(4);
                    remain.remain_fee = rs.getInt(5);
                    remainBTreeMap.put(remain.loan_order_no,remain);
                }
                return null;
            }
        }).run();

        f_seq.get(60, TimeUnit.MINUTES);
        f_repay.get(60, TimeUnit.MINUTES);
        f_remain.get(60, TimeUnit.MINUTES);
        f_overdue.get(60, TimeUnit.MINUTES);
        //关联处理
        try{
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            while(rs.next()){
                List<String> values= new ArrayList<>();
                //id
                values.add(String.valueOf(rs.getLong("id")));
                String loan_order_no = rs.getString("loan_order_no");
                values.add(loan_order_no);
                values.add(rs.getString("third_order_no"));
                values.add(String.valueOf(rs.getLong("customer_id")));
                values.add(String.valueOf(rs.getInt("store_id")));
                values.add(String.valueOf(rs.getInt("loan_type")));
                //loan_seq
                LoanSeq seq = seqBTreeMap.get(loan_order_no);
                values.add(seq==null?null:String.valueOf(seq.loan_seq));
                values.add(String.valueOf(rs.getInt("product_type")));
                values.add(String.valueOf(rs.getInt("status")));
                values.add(rs.getString("failure_msg"));
                int principal = rs.getInt("principal");
                values.add(String.valueOf(principal));
                values.add(String.valueOf(rs.getInt("free_interest_day")));
                values.add(String.valueOf(rs.getDouble("interest_rate")));
                values.add(String.valueOf(rs.getDouble("fee_rate")));
                values.add(rs.getString("fund_channel"));
                Remain remain = remainBTreeMap.get(loan_order_no);
                values.add(remain==null?null:Double.toString(remain.principal_damages_rate));
                RepaySum repay = repayBTreeMap.get(loan_order_no);
                int remain_principal = repay ==null? principal:principal-repay.repay_principal;
                values.add(String.valueOf(remain_principal));
                int remain_fee = repay == null? 0:repay.repay_fee;
                values.add(String.valueOf(remain_fee));
                /**
                 * CASE
                 WHEN biz_type = 1 THEN (should_interest - IFNULL(repay_interest, 0))
                 ELSE remain_interest
                 END AS remain_interest,
                 CASE
                 WHEN biz_type = 1 THEN (should_damages - IFNULL(repay_damages, 0))
                 ELSE remain_damages
                 END AS remain_damages,
                 */
                if(remain!=null && repay!=null){
                    int remain_damages = remain.remain_damages;
                    int product_type = rs.getInt("product_type");
                    if(product_type==1){
                        Integer should_damages = overdueBTreeMap.get(loan_order_no);
                        remain_damages = should_damages == null? 0:repay.repay_fee;
                    }
                    values.add(String.valueOf(remain_damages));
                }else{
                    values.add(null);
                }

                values.add(seq==null?null:seq.loan_time);
                if(repay!=null){
                    String finish_time = principal - repay.repay_principal<=0? repay.finish_time:null;
                    values.add(finish_time);
                }else{
                    values.add(null);
                }
                values.add(sdf.format(rs.getDate("create_time")));
                values.add(sdf.format(rs.getDate("update_time")));

                FileHelper.writeLine(fs,"\001",values.toArray(new String[0]));
            }
            fs.flush();
        }finally {
            if(fs!=null){
                fs.close();
            }
            rs.close();
        }
        db.close();
    }

    @Override
    String getWork_fileName() {
        return work_fileName;
    }
}
