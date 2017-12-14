package com.dafy.skye.mapper;

/**
 * Created by quanchengyun on 2017/11/3.
 */
public class FLoanOrderMapperStatement {

    public static final String create_table =
            "create table f_loan_order( " +
            " id BIGINT," +
            " loan_order_no STRING," +
            " third_order_no STRING," +
            " customer_id BIGINT," +
            " store_id INT," +
            " loan_type TINYINT," +
            " loan_seq INT," +
            " product_type TINYINT," +
            " status TINYINT," +
            " failure_msg STRING," +
            " principal INT," +
            " free_interest_day INT," +
            " interest_rate DECIMAL," +
            " fee_rate DECIMAL," +
            " fund_channel STRING," +
            " principal_damages_rate DECIMAL," +
            " remain_principal INT," +
            " remain_fee INT," +
            " remain_damages INT," +
            " loan_time TIMESTAMP," +
            " finish_time TIMESTAMP," +
            " create_time TIMESTAMP," +
            " update_time TIMESTAMP" +
            " )" +
            " ROW FORMAT DELIMITED FIELDS TERMINATED BY '\\001' lines terminated by '\\n' location '/user/hive/warehouse/sevend.db/f_loan_order'";

    public static final String drop_table = "drop table if exists f_loan_order";

    public static final String drop_backup_table ="drop table if exists sevend{date}.f_loan_order";

    public static final String backup ="create table  sevend{date}.f_loan_order location '/user/hive/warehouse/sevend{date}.db/f_loan_order' as select * from sevend.f_loan_order ";

    public static  final String load_data = "load data inpath ? into table f_loan_order";


    public static final String get_main_data = 
            "SELECT a.id ," +
            " a.order_no AS loan_order_no," +
            " b.dafy_withdraw_id AS third_order_no," +
            " a.customer_id AS customer_id," +
            " a.store_id AS store_id," +
            " a.repay_type AS loan_type," +
            " a.biz_type AS product_type," +
            " a.order_status AS status," +
            " a.error_msg AS failure_msg," +
            " a.principal AS principal," +
            " a.interest_type AS free_interest_day," +
            " a.year_interest_rate AS interest_rate," +// 现金贷存的是年化率，其他几个产品存的是月利率
            " a.service_fee_rate AS fee_rate," + // 服务费率- 合伙人才有
            " a.pay_channel AS fund_channel, " +
            " a.create_time AS create_time, " +
            " a.update_time AS update_time "+
            " FROM " +
            " t_loan_order a" +
            " LEFT JOIN t_withdraw_gateway_order b ON cast(a.order_no as char) = b.biz_order_id";


    public static final String get_calculate_data_loanSeq =
            "SELECT " +
            " merge_table1.order_no AS loan_order_no," +
            " loan_time," +
            " loan_seq " +
            " FROM " +
            " (" +
            " SELECT" +
            " customer_id," +
            " order_no," +
            " success_time AS loan_time," +
            " principal," +
            " biz_type," +
            " IF" +
            " ( @pdept = b.customer_id, @rank := @rank + 1, @rank := 1 ) AS loan_seq," +
            " @pdept := b.customer_id AS customer_id_copy " +
            " FROM" +
            " ( SELECT customer_id, order_no, success_time, principal, biz_type FROM t_loan_order_ok WHERE state = 1 ORDER BY customer_id, success_time ) b," +
            " ( SELECT @rownum := 0, @pdept := NULL, @rank := 0 ) c " +
            " ) merge_table1";

    public static final String get_calculate_data_repaySum =
            "SELECT " +
            "    loan_order_ok_no AS loan_order_no," +
            "    SUM(principal) AS repay_principal," +
            "    MAX(create_time) AS finish_time," +
            "    SUM(interest) AS repay_interest," +
            "    SUM(damages) AS repay_damages," +
            "    SUM(fee) AS repay_fee " +
            " FROM " +
            "    t_repay_ok_info " +
            " GROUP BY loan_order_ok_no";

    public static final String getGet_calculate_data_overdue =
            "SELECT " +
            "    SUBSTRING_INDEX(order_no, '-', 1) AS loan_order_no," +
            "    SUM(damages_money) AS should_damages " +
            " FROM " +
            "    t_overdue_water " +
            " GROUP BY SUBSTRING_INDEX(order_no, '-', 1)";


    public static final String getGet_calculate_data_interest =
            "";


    public static final String getGet_calculate_data_remain =
            "SELECT " +
            "    origin_order_no AS loan_order_no, " +
            "    principal_year_damages_rate AS principal_damages_rate, " +
            "    SUM(remain_interest) AS remain_interest, " +
            "    SUM(remain_damages) AS remain_damages, " +
            "    SUM(remain_fee) AS remain_fee " +
            " FROM " +
            "    t_repay_plan_order " +
            " GROUP BY origin_order_no ";


}
