package com.dafy.skye.mapper;

/**
 * Created by quanchengyun on 2017/10/25.
 */
public class RegionMapperStatement {

    //创建区域表
    public static final String create_table="create table d_region(id int,province string,city string)" +
            "ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' lines terminated by '\\n' location '/user/hive/warehouse/sevend.db/d_region'";

    public static final String drop_table="DROP TABLE IF EXISTS d_region";

    public static final String load_data="load data inpath ? into table d_region";

    public static  final String get_all_regions = "select id,city from d_region";

    public static final String backup ="create table sevend{date}.d_region location '/user/hive/warehouse/sevend{date}.db/d_region' as select * from sevend.d_region";


}
