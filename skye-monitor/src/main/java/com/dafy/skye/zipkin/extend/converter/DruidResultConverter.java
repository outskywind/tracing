package com.dafy.skye.zipkin.extend.converter;

import com.dafy.skye.druid.entity.GroupbyQueryResult;
import com.dafy.skye.druid.entity.TimeSeriesQueryResult;
import com.dafy.skye.zipkin.extend.dto.MonitorMetric;
import com.dafy.skye.zipkin.extend.dto.SeriesMetric;
import com.dafy.skye.zipkin.extend.dto.TimeSeriesResult;
import com.dafy.skye.zipkin.extend.util.TimeUtil;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.math.BigDecimal;
import java.util.*;

/**
 * Created by quanchengyun on 2018/5/16.
 */
public class DruidResultConverter {

    /**
     * 是druid时序维度的名字 dimension
     * @param series
     * @param seriesKey
     * @return
     */
    public static Collection<TimeSeriesResult> convertSeriesMetricGroupby(List<GroupbyQueryResult> series, String seriesKey, String timeInterval){

        Map<String,TimeSeriesResult> holder = new HashMap<>();
        //TODO
        for(GroupbyQueryResult result:series){
            String name = (String)result.getEvent().get(seriesKey);
            long avg_latency = result.getEvent().get("avg_latency")==null?0: Math.round((Double)(result.getEvent().get("avg_latency")));
            long count = result.getEvent().get("_count")==null?0:(Integer)(result.getEvent().get("_count"));
            long avg_qps = count/ TimeUtil.parseTimeIntervalSeconds(timeInterval);

            TimeSeriesResult holdResult = holder.get(name);
            if(holdResult==null){
                holdResult = new TimeSeriesResult();
                holder.put(name,holdResult);
                holdResult.setName(name);
                List<SeriesMetric>  seriesMetrics = new ArrayList<>();
                holdResult.setSeries(seriesMetrics);
            }
            String timestamp = result.getTimestamp();
            DateTimeFormatter format = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
            //时间解析
            DateTime dateTime = DateTime.parse(timestamp, format);
            holdResult.getSeries().add(new SeriesMetric(dateTime.getMillis(),avg_qps,avg_latency,count));
        }
        return holder.values();
    }

    public static Collection<SeriesMetric> convertSeriesMetricTimeseries(List<TimeSeriesQueryResult> series,String timeInterval){

        List<SeriesMetric>  seriesMetrics = new ArrayList<>();
        //TODO
        for(TimeSeriesQueryResult result:series){
            long avg_latency = result.getResult().get("avg_latency")==null?0: Math.round((Double)(result.getResult().get("avg_latency")));
            long count = result.getResult().get("_count")==null?0:(Integer)(result.getResult().get("_count"));
            //div
            double avg_qps = div(count,TimeUtil.parseTimeIntervalSeconds(timeInterval),2);
            String timestamp = result.getTimestamp();
            DateTimeFormatter format = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
            //时间解析
            DateTime dateTime = DateTime.parse(timestamp, format);
            seriesMetrics.add(new SeriesMetric(dateTime.getMillis(),avg_qps,avg_latency,count));
        }
        return seriesMetrics;
    }


    private static double div(long div1, long div2, int scale){
        BigDecimal bigDecimal = new BigDecimal(div1);
        BigDecimal bigDecimal2 = new BigDecimal(div2);
        return bigDecimal.divide(bigDecimal2, scale, BigDecimal.ROUND_HALF_UP).doubleValue();
    }



    public static Collection<MonitorMetric> convertMetric(List<GroupbyQueryResult> series, String seriesKey){

        Map<String,MonitorMetric> holder = new HashMap<>();
        //TODO
        for(GroupbyQueryResult result:series){
            String name = (String)result.getEvent().get(seriesKey);
            long avg_latency = result.getEvent().get("avg_latency")==null?0: Math.round((Double)(result.getEvent().get("avg_latency")));
            long count = result.getEvent().get("_count")==null?0:(Integer)(result.getEvent().get("_count"));
            long peak_qps = result.getEvent().get("peak_qps")==null?0:(Integer)(result.getEvent().get("peak_qps"));
            double success_rate = result.getEvent().get("success_rate")==null?0:(Double)(result.getEvent().get("success_rate"));
            MonitorMetric holdResult = holder.get(name);
            if(holdResult==null){
                holdResult = new MonitorMetric();
                holder.put(name,holdResult);
                holdResult.setName(name);
            }
            holdResult.setPeak_qps(peak_qps);
            holdResult.setLatency(avg_latency);
            holdResult.setSuccess_rate(Math.round(success_rate*100)+"%");
            holdResult.setSuccessPercent(Math.round(success_rate*100));
            holdResult.setCount(count);
        }
        return holder.values();
    }
}
