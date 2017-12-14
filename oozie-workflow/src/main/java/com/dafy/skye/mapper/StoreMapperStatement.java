package com.dafy.skye.mapper;

/**
 * Created by quanchengyun on 2017/10/26.
 */
public class StoreMapperStatement {

        //创建hive门店表
        public static final String create_table="create table d_store(id int,name string,region_id int)" +
                "ROW FORMAT DELIMITED FIELDS TERMINATED BY '\\t' lines terminated by '\\n' location '/user/hive/warehouse/sevend.db/d_store'";

        public static final String drop_table="DROP TABLE IF EXISTS d_store";

        public static final String load_data="load data inpath ? into table d_store";

        //查询mysql表
        public static final String get_store = "select id, name ,province,city,update_time from t_aid_store";

        public static final String drop_backup_table ="drop table if exists sevend{date}.t_aid_store";

        public static final String backup ="create table  sevend{date}.d_store location '/user/hive/warehouse/sevend{date}.db/d_store' as select * from sevend.d_store ";

}
