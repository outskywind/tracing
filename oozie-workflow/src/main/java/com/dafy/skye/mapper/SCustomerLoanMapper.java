package com.dafy.skye.mapper;

/**
 * Created by quanchengyun on 2017/12/6.
 */
public class SCustomerLoanMapper {
    //
    public static final String create_table="create table s_customer_loan(customer_id bigint, " +
            "biz_type TINYINT, "+
            "overdue_loan_order_num INT, " +
            "overdue_loan_amt INT, " +
            "overdue_day INT, " +
            "avg_loan_duration INT, " +
            "avg_loan_amt INT, " +
            "last_loan_time TIMESTAMP, " +
            "last_repay_time TIMESTAMP, " +
            "loan_num INT, " +
            "loan_amount INT, " +
            "on_loan INT, " +
            "on_loan_days INT, " + //空着
            "repay_num INT, " +
            "repay_amount INT)" +
            "ROW FORMAT DELIMITED FIELDS TERMINATED BY '\\001' lines terminated by '\\n' location '/user/hive/warehouse/sevend.db/s_customer_loan'";

    public static final String drop_table="DROP TABLE IF EXISTS s_customer_loan";

    public static final String load_data="load data inpath ? into table s_customer_loan";

    public static final String drop_backup_table ="drop table if exists sevend{date}.s_customer_loan";

    public static final String backup ="create table  sevend{date}.s_customer_loan location '/user/hive/warehouse/sevend{date}.db/s_customer_loan' as select * from sevend.s_customer_loan ";


    public static final String get_main_data ="SELECT " +
            "   t1.id AS customer_id " +
            " FROM " +
            "   t_customer t1" ;


    public static final String get_loan_data = "select customer_id, biz_type, " +
            " count(order_no) as loan_num," +
            " sum(loan_money) as loan_amount," +
            " sum(loan_money) div count(order_no) as avg_loan_amt," +
            " sum(repay_times) as repay_num, " +
            " sum(repay_money) as repay_amount," +
            " sum(case when on_loan_money<=0 then on_loan_money else 0 end) as on_loan," +
            " max(loan_date) as last_loan_time, " +
            " max(repay_date) as last_repay_time, " +
            " count(case when on_loan_money<=0 then 1 else 0 end) as repay_off_count, " +
            " sum(case when on_loan_money<=0 then DATEDIFF(date(repay_date),date(loan_date)) else 0 end) as repay_off_days, " +
            " sum(case when on_loan_money<=0 then DATEDIFF(date(repay_date),date(loan_date)) else 0 end) div count(case when on_loan_money<=0 then 1 else 0 end)as avg_loan_duration " +
            " from (" +
            " SELECT " +
            "    t1.customer_id," +
            "    t1.biz_type," +
            "    t1.order_no," +
            "    t1.principal AS loan_money," +
            "    t1.success_time AS loan_date," +
            "    ifnull(sum(t2.principal),0) AS repay_money," +
            "    count(1) as repay_times," +
            "    t2.create_time as repay_date," +
            "    t1.principal -ifnull(sum(t2.principal),0) as on_loan_money" +
            " FROM " +
            "    t_loan_order_ok t1 force index (idx_order_no) LEFT JOIN t_repay_ok_info t2" +
            "    ON t1.order_no = t2.loan_order_ok_no " +
            " WHERE " +
            "    t2.create_time < timestamp(CURDATE()) " +
            " GROUP BY t1.order_no) t3 group by customer_id, biz_type " +
            " UNION ALL " +
            "select customer_id, " +
            "11 as biz_type, "+
            "count(order_no) as loan_num," +
            "sum(loan_money) as loan_amount," +
            "sum(loan_money) div count(order_no) as avg_loan_amt," +
            "sum(repay_times) as repay_num, " +
            "sum(repay_money) as repay_amount," +
            "sum(case when on_loan_money<=0 then 0 else on_loan_money end) as on_loan," +
            "max(loan_date) as last_loan_time, " +
            "max(repay_date) as last_repay_time," +
            "count(case when on_loan_money<=0 then 1 else 0 end) as repay_off_count, " +
            "sum(case when on_loan_money<=0 then DATEDIFF(date(repay_date),date(loan_date)) else 0 end) as repay_off_days," +
            "sum(case when on_loan_money<=0 then DATEDIFF(date(repay_date),date(loan_date)) else 0 end) div count(case when on_loan_money<=0 then 1 else 0 end)as avg_loan_duration" +
            " from (" +
            " SELECT " +
            "    t1.customer_id," +
            "    t1.order_no," +
            "    t1.principal AS loan_money," +
            "    t1.success_time AS loan_date," +
            "    ifnull(sum(t2.principal),0) AS repay_money," +
            "    count(1) as repay_times," +
            "    t2.create_time as repay_date," +
            "    t1.principal -ifnull(sum(t2.principal),0) as on_loan_money" +
            " FROM " +
            "    t_policyloan_loan_order_ok t1 force index(idx_order_no) LEFT JOIN t_policyloan_repay_ok_info t2 " +
            "    ON t1.order_no = t2.loan_order_no " +
            " WHERE " +
            "    t2.create_time < timestamp(curdate()) " +
            " GROUP BY t1.order_no) t3 group by customer_id";


    public static final String get_overdue_data = "SELECT " +
            "    customer_id," +
            "    biz_type, " +
            "    COUNT(DISTINCT origin_order_no) AS overdue_loan_order_num, " +
            "    SUM(principal) AS overdue_loan_amt, " +
            "    COUNT(DISTINCT damages_date) AS overdue_day " +
            "FROM " +
            "    t_repay_plan_order " +
            "WHERE " +
            "    damages > 0 " +
            "GROUP BY customer_id , biz_type  " +
            "UNION ALL  " +
            "SELECT  " +
            "    customer_id, " +
            "    11 as biz_type, " +
            "    COUNT(DISTINCT origin_order_no) AS overdue_loan_order_num, " +
            "    SUM(principal) AS overdue_loan_amt, " +
            "    COUNT(DISTINCT damages_date) AS overdue_day " +
            " FROM " +
            "    t_policyloan_repay_plan_order " +
            " WHERE " +
            "    damages > 0 " +
            " GROUP BY customer_id";



    public static final String get_loan_original="";


}
