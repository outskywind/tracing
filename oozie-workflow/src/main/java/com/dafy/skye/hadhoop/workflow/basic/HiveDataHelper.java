package com.dafy.skye.hadhoop.workflow.basic;

import com.dafy.skye.component.Connector;
import com.dafy.skye.component.LogBuilder;
import com.dafy.skye.mapper.RegionMapperStatement;
import com.dafy.skye.mapper.StoreMapperStatement;
import org.slf4j.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by quanchengyun on 2017/10/31.
 */
public class HiveDataHelper {
    private static Logger log = LogBuilder.buildLogger(HiveDataHelper.class);
    /**
     *
     * @return
     */
    public static Map<String,Integer> prepareRegionData(Connector hiveConnector,Connection conn){
        ResultSet regions = null;
        Map<String,Integer> regionMap = new HashMap<>();
        try {
            regions = hiveConnector.query(conn, RegionMapperStatement.get_all_regions);
            int regionId=0;
            String city = "";
            while(regions.next()){
                regionId = regions.getInt(1);
                city = regions.getString(2);
                regionMap.put(city,regionId);
                log.debug("{},{}",regionId,city);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return regionMap;
    }
}
