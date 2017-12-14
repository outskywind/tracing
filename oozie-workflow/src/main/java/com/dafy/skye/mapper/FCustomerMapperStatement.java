package com.dafy.skye.mapper;

/**
 * Created by quanchengyun on 2017/10/30.
 */
public class FCustomerMapperStatement {

    public static final String create_table="create table if not exists f_customer(" +
            "    id BIGINT," +
            "    id_no STRING," +
            "    name STRING," +
            "    register_mobile STRING," +
            "    type TINYINT," +
            "    id_region_id INT," +
            "    mobile_region_id INT," +
            "    census_region_id INT," +
            "    apply_region_id INT," +
            "    live_region_id INT," +
            "    company STRING," +
            "    company_region_id INT," +
            "    gender INT," +
            "    birth_date DATE," +
            "    age INT," +
            "    bank_mobile STRING," +
            "    bank_card_no STRING," +
            "    bank_code STRING," +
            "    register_channel STRING," +
            "    invite_id BIGINT," +
            "    recommender_id BIGINT," +
            "    maintenance_id BIGINT," +
            "    interviewer_id BIGINT," +
            "    create_time TIMESTAMP," +
            "    update_time TIMESTAMP," +
            "    quota_time TIMESTAMP," +
            "    quota INT, "+
            "    remain_quota INT, "+
            "    credit_status TINYINT, "+
            "    submit_time TIMESTAMP, "+
            "    store_id INT"+
            "    )" +
            "    ROW FORMAT DELIMITED FIELDS TERMINATED BY '\\001' lines terminated by '\\n' location '/user/hive/warehouse/sevend.db/f_customer'";


    public static final String drop_table ="drop table if exists f_customer";

    public static final String drop_backup_table ="drop table if exists sevend{date}.f_customer";

    public static final String backup ="create table sevend{date}.f_customer location '/user/hive/warehouse/sevend{date}.db/f_customer' as select * from sevend.f_customer ";

    /**
     * 为了简单起见，先决定每次重新全表导入
     */
    public static  final String load_data = "load data inpath ? into table f_customer";

    //查询mysql表
    public static final String get_customers_info = "SELECT " +
            "    c.id," +
            "    b.id_no," +
            "    c.name," +
            "    c.mobile AS register_mobile," +
            "    c.customer_type AS type," +
            "    ex.residential_area," +
            "    ex.job_company as company_name," +
            "    ex.job_area as company_area," +
            "    b.bind_phone," +
            "    b.card_no," +
            "    b.bank_code," +
            "    c.channel," +
            "    c.be_invite_id," +
            "    c.recommender_id," +
            "    ex.aid_customer_id AS maintenance_id," +
            "    ex.interviewer_id," +
            "    c.create_time," +
            "    c.update_time, " +
            "    ex.real_name_callback_audit_time as quota_time, "+
            "    ex.audit_system_real_name_status as credit_status, "+
            "    ex.real_name_submit_audit_time AS submit_time, "+
            "    cq.whole_quota as quota, "+
            "    cq.remaining_quota as remain_quota, "+
            "    ex.store_id "+
            " FROM" +
            "    t_customer c" +
            "        LEFT JOIN" +
            "    t_bank_card b ON c.id = b.customer_id AND b.state = 1" +
            "        AND b.is_security_card = 1" +
            "        AND b.real_name_call_back_status = 1" +
            "        LEFT JOIN" +
            "    t_customer_extra_info ex ON c.id = ex.customer_id AND ex.state=1" +
            "    LEFT JOIN "+
            "    t_customer_quota cq ON c.id=cq.customer_id AND cq.state=1"+
            " WHERE " +
            "    c.customer_type = 1 " +
            " UNION ALL " +
            " SELECT " +
            "    c.id," +
            "    b.id_no," +
            "    c.name," +
            "    c.mobile AS register_mobile," +
            "    c.customer_type AS type," +
            "    ex.residential_area," +
            "    ex.company_name," +
            "    ex.gps_area as company_area," +
            "    b.bind_phone," +
            "    b.card_no," +
            "    b.bank_code," +
            "    c.channel," +
            "    c.be_invite_id," +
            "    c.recommender_id," +
            "    ex.aid_customer_id AS maintenance_id," +
            "    ex.interviewer_id," +
            "    c.create_time," +
            "    c.update_time," +
            "    ex.real_name_callback_audit_time as quota_time, "+
            "    ex.audit_system_real_name_status as credit_status, "+
            "    ex.real_name_submit_audit_time AS submit_time, "+
            "    cq.whole_quota as quota, "+
            "    cq.remaining_quota as remain_quota, "+
            "    ex.store_id "+
            " FROM " +
            "    t_customer c" +
            "        LEFT JOIN" +
            "    t_bank_card b ON c.id = b.customer_id AND b.state = 1" +
            "        AND b.is_security_card = 1" +
            "        AND b.real_name_call_back_status = 1" +
            "        LEFT JOIN " +
            "    t_policyloan_credit_info ex ON c.id = ex.customer_id AND ex.state=1 " +
            "    LEFT JOIN "+
            "    t_customer_quota cq ON c.id=cq.customer_id AND cq.state=1"+
            " WHERE " +
            "    c.customer_type = 2";

}
