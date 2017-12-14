package com.dafy.skye.mapper;

/**
 * Created by quanchengyun on 2017/11/9.
 */
public class FRepayOrderMapperStatement {

    public  static final String create_table =
            "create table if not exists sevend.f_repay_order(" +
                    " loan_order_no string, " +
                    " repay_order_no string, " +
                    " customer_id bigint, " +
                    " principal int, " +
                    " interest int, " +
                    " fee int, " +
                    " damages int, " +
                    " status tinyint, " +
                    " loan_type tinyint, " +
                    " product_type tinyint, " +
                    " order_type tinyint, " +
                    " initiator tinyint, " +
                    " failure_code string, " +
                    " failure_msg string, " +
                    " repay_time timestamp, " +
                    " repay_days_after_loan int, " +
                    " state tinyint, " +
                    " create_time timestamp, " +
                    " update_time timestamp " +
                    ") " +
                    "ROW FORMAT DELIMITED FIELDS TERMINATED BY '\\001' lines terminated by '\\n' location '/user/hive/warehouse/sevend.db/f_repay_order'";


    public static final String drop_table = "drop table if exists sevend.f_repay_order";

    public static final String drop_backup_table ="drop table if exists sevend{date}.f_repay_order";

    public static final String backup ="create table  sevend{date}.f_repay_order location '/user/hive/warehouse/sevend{date}.db/f_repay_order' as select * from sevend.f_repay_order ";

    public static  final String load_data = "load data inpath ? into table sevend.f_repay_order";

    public static final String get_main_data =
            "SELECT  " +
            "    b.loan_order_ok_no AS loan_order_no, " +
            "    a.water_no AS repay_order_no, " +
            "    c.dafy_withhold_id AS third_order_no, " +
            "    a.customer_id AS customer_id, " +
            "    IFNULL(b.principal, a.principal) AS principal, " +
            "    IFNULL(b.interest, a.interest) AS interest, " +
            "    IFNULL(b.fee, a.fee) AS fee, " +
            "    IFNULL(b.damages, a.damages) AS damages, " +
            "    a.repay_status AS status, " +
            "    a.biz_type AS product_type, " +
            "    a.order_type AS order_type, " +
            "    a.initiator AS initiator, " +
            "    a.error_code AS failure_code, " +
            "    a.error_msg AS failure_msg, " +
            "    b.create_time AS repay_time, " +
            "    a.state AS state, " +
            "    DATEDIFF(b.create_time, d.success_time) + 1 AS repay_days_after_loan, " +
            "    a.create_time as create_time, " +
            "    a.update_time as update_time, " +
            " FROM " +
            "    t_repay_water a " +
            "        LEFT JOIN " +
            "    t_repay_ok_info b ON a.water_no = b.repay_water_no " +
            "        LEFT JOIN " +
            "    t_withhold_gateway_order c ON CAST(a.water_no AS CHAR) = c.biz_order_id " +
            "        LEFT JOIN " +
            "    t_loan_order_ok d ON b.loan_order_ok_no = d.order_no";


}
