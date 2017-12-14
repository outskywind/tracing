package com.dafy.skye.hadhoop.workflow.action;

import com.dafy.skye.component.*;
import com.dafy.skye.mapper.SCustomerLoanMapper;
import com.dafy.skye.model.Result;
import com.dafy.skye.model.SCustomerLoan;
import com.dafy.skye.model.SCustomerOverdue;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;
import org.mapdb.serializer.SerializerArrayTuple;
import org.slf4j.Logger;

import java.io.DataOutputStream;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Created by quanchengyun on 2017/12/6.
 */
public class SCustomerLoanImport extends  BasicOozieAction {

    private static Logger log = LogBuilder.buildLogger(StoreImport.class);
    //清洗后数据保存的hdfs中间文件名
    private static final String work_fileName = "sCustomerLoan";

    @Override
    protected void postAction() throws Exception{
        super.postAction();
        String date = DateUtil.getDateformat();
        hiveConnector.execute(hiveconn, SCustomerLoanMapper.drop_backup_table.replaceAll("\\{date\\}",date));
        hiveConnector.execute(hiveconn, SCustomerLoanMapper.backup.replaceAll("\\{date\\}",date));
    }

    @Override
    Result _doAction(Map configs) {
        try{
            ResultSet rs  = mysqlConnector.query(mysqlconn, SCustomerLoanMapper.get_main_data,null);
            processOriginalData(rs);
            //
            hiveConnector.execute(hiveconn, SCustomerLoanMapper.drop_table);
            hiveConnector.execute(hiveconn, SCustomerLoanMapper.create_table);
            //then load the data in local file system
            hiveConnector.insertOrUpdate(hiveconn,SCustomerLoanMapper.load_data, work_dir+"/"+work_fileName);
        }catch(Exception e){
            log.error("导入出错：",e);
        }
        return null;
    }

    private void processOriginalData(ResultSet rs) throws Exception{

        DataOutputStream fs = getHDFSWriter();
        DB db = DBMaker.memoryDB().make();
        //loan 处理
        final NavigableSet<Object[]> loanMultimap = db.treeSet("loan")
                //set tuple serializer
                .serializer(new SerializerArrayTuple(Serializer.STRING, Serializer.JAVA))
                .counterEnable()
                .counterEnable()
                .counterEnable()
                .createOrOpen();

        Future s_loan = JDBCHelper.build().withQuery(new QueryProcessor() {
            @Override
            public ResultSet query() throws Exception{
                return mysqlConnector.query(mysqlconn, SCustomerLoanMapper.get_loan_data,null);
            }
        }).withResultSetProcessor(new ResultSetProcessor() {
            @Override
            public <T> T process(ResultSet rs) throws Exception{
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                while(rs.next()){
                    SCustomerLoan cur = new SCustomerLoan();
                    long customer_id = rs.getLong("customer_id");
                    cur.biz_type = rs.getInt("biz_type");
                    cur.loan_num =  rs.getInt("loan_num");
                    cur.loan_amount = rs.getInt("loan_amount");
                    cur.avg_loan_amt = rs.getInt("avg_loan_amt");
                    cur.repay_num =  rs.getInt("repay_num");
                    cur.repay_amount = rs.getInt("repay_amount");
                    cur.on_loan = rs.getInt("on_loan");
                    cur.last_loan_time =  rs.getDate("last_loan_time")==null?null:sdf.format(rs.getDate("last_loan_time"));
                    cur.last_repay_time = rs.getDate("last_repay_time")==null?null:sdf.format(rs.getDate("last_repay_time"));
                    cur.repay_off_count = rs.getInt("repay_off_count");
                    cur.repay_off_days =  rs.getInt("repay_off_days");
                    cur.avg_loan_duration = rs.getInt("avg_loan_duration");
                    loanMultimap.add(new Object[]{customer_id,cur});
                }
                return null;
            }
        }).run();

        //overdue 处理
        final NavigableSet<Object[]> overdueMultimap = db.treeSet("overdue")
                //set tuple serializer
                .serializer(new SerializerArrayTuple(Serializer.STRING, Serializer.JAVA))
                .counterEnable()
                .counterEnable()
                .counterEnable()
                .createOrOpen();

        Future s_overdue = JDBCHelper.build().withQuery(new QueryProcessor() {
            @Override
            public ResultSet query() throws Exception{
                return mysqlConnector.query(mysqlconn, SCustomerLoanMapper.get_overdue_data,null);
            }
        }).withResultSetProcessor(new ResultSetProcessor() {
            @Override
            public <T> T process(ResultSet rs) throws Exception{
                while(rs.next()){
                    SCustomerOverdue cur = new SCustomerOverdue();
                    long customer_id = rs.getLong("customer_id");
                    cur.biz_type = rs.getInt("biz_type");
                    cur.overdue_day = rs.getInt("overdue_day");
                    cur.overdue_loan_amt = rs.getInt("overdue_loan_amt");
                    cur.overdue_loan_order_num = rs.getInt("overdue_loan_order_num");
                    overdueMultimap.add(new Object[]{customer_id,cur});
                }
                return null;
            }
        }).run();

        s_loan.get(30, TimeUnit.MINUTES);
        s_overdue.get(30, TimeUnit.MINUTES);

        //关联到主表.不是11对应的
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            while (rs.next()) {
                //id
                long customer_id = rs.getLong("customer_id");

                //loan
                Set loanSubset = loanMultimap.subSet(
                        new Object[]{customer_id},         // lower interval bound
                        new Object[]{customer_id, null});
                //overdue
                Set overdueSubset = overdueMultimap.subSet(
                        new Object[]{customer_id},         // lower interval bound
                        new Object[]{customer_id, null});

                Set<Integer> biz_type_count = new HashSet<>();
                for(Object record :loanSubset){
                    biz_type_count.add(((SCustomerLoan)record).biz_type);
                }
                for(Object record :overdueSubset){
                    biz_type_count.add(((SCustomerOverdue)record).biz_type);
                }

                if(biz_type_count.size()>0){
                    //关联后多条
                    for(int bize_type : biz_type_count){
                        List<String> record = new ArrayList<>();
                        //关联填充值
                        record.add(String.valueOf(customer_id));
                        record.add(String.valueOf(bize_type));
                        //遍历biz_type 获取字段
                        SCustomerLoan loan_record = null;
                        for(Object loan :loanSubset){
                            if(bize_type ==((SCustomerLoan)loan).biz_type){
                                loan_record = (SCustomerLoan)loan;
                                break;
                            }
                        }
                        SCustomerOverdue overdue_record = null;
                        for(Object overdue :overdueSubset){
                            if(bize_type ==((SCustomerOverdue)overdue).biz_type){
                                overdue_record = (SCustomerOverdue)overdue;
                                break;
                            }
                        }
                        String overdue_loan_order_num = null;
                        String overdue_loan_amt = null;
                        String overdue_day = null;
                        String avg_loan_duration = null;
                        String avg_loan_amt = null;
                        String last_loan_time = null;
                        String last_repay_time = null;
                        String loan_num = null;
                        String loan_amount = null;
                        String on_loan = null;
                        String on_loan_days = null;
                        String repay_num = null;
                        String repay_amount = null;

                        if(loan_record!=null){
                            avg_loan_duration = String.valueOf(loan_record.avg_loan_duration);
                            avg_loan_amt = String.valueOf(loan_record.avg_loan_amt);
                            last_loan_time = loan_record.last_loan_time;
                            last_repay_time = loan_record.last_repay_time;
                            loan_num = String.valueOf(loan_record.loan_num);
                            loan_amount = String.valueOf(loan_record.loan_amount);
                            on_loan = String.valueOf(loan_record.on_loan);
                            repay_num = String.valueOf(loan_record.repay_num);
                            repay_amount = String.valueOf(loan_record.repay_amount);
                        }
                        if(overdue_record!=null){
                            overdue_loan_order_num = String.valueOf(overdue_record.overdue_loan_order_num);
                            overdue_loan_amt = String.valueOf(overdue_record.overdue_loan_amt);
                            overdue_day = String.valueOf(overdue_record.overdue_day);
                        }
                        record.add(String.valueOf(loan_record.avg_loan_duration));
                        record.add(String.valueOf(loan_record.avg_loan_amt));
                        record.add(loan_record.last_loan_time);
                        record.add(loan_record.last_repay_time);
                        record.add(String.valueOf(loan_record.loan_num));
                        record.add(String.valueOf(loan_record.loan_amount));
                        record.add(String.valueOf(loan_record.on_loan));
                        record.add(null);//on_loan_days
                        record.add(String.valueOf(loan_record.repay_num));
                        record.add(String.valueOf(loan_record.repay_amount));
                        //写fs
                        FileHelper.writeLine(fs,"\001",record.toArray(new String[0]));
                    }
                }else{
                    List<String> record = new ArrayList<>();
                    FileHelper.writeLine(fs,"\001",record.toArray(new String[0]));
                }
                fs.flush();
            }
        }finally {
            if(fs!=null){
                fs.close();
            }
            rs.close();
            db.close();
        }

    }

    @Override
    String getWork_fileName() {
        return work_fileName;
    }
}
