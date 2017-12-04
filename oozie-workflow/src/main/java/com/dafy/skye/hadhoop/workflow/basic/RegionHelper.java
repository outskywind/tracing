package com.dafy.skye.hadhoop.workflow.basic;

import com.dafy.skye.component.Connector;
import com.dafy.skye.component.LogBuilder;
import org.slf4j.Logger;

import java.sql.Connection;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by quanchengyun on 2017/10/31.
 */
public class RegionHelper {
    private static Logger log = LogBuilder.buildLogger(RegionHelper.class);
    //唯一的region缓存
    private static Map<String,Integer> regionMap = null;
    //相当于是普通的市级，但是编码结尾不是00
    public static final List<String> specialCityFlag = Arrays.asList("省直辖县级行政区划","自治区直辖县级行政区划");

    public static RegionHelper getInstance(Connector hiveConnector,Connection conn){
        if(regionMap==null){
            regionMap = HiveDataHelper.prepareRegionData(hiveConnector,conn);
        }
        return new RegionHelper();
    }

    public String getRegionId(String city){
        if(regionMap==null){
            throw new IllegalStateException("regionMap 尚未初始化.");
        }
        int regionId = 0;
        for(String cityKey : regionMap.keySet()){
            //假设地名都是唯一的，只是带“市”后缀
            //cityKey是完整的名称
            if(cityKey.startsWith(city)){
                regionId = regionMap.get(cityKey);
                return String.valueOf(regionId);
            }else{
                //格式化地区
                if(city!=null && city.length()>=4 && cityKey.startsWith(city.substring(0,city.length()-2))){
                    return String.valueOf(regionId);
                }
            }
        }
        if(regionId==0){
            log.warn("city {} 没有在d_region表匹配到",city);
        }
        return null;
    }


}
