package com.dafy.skye.zipkin.extend.job;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.simple.SimpleJob;
import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.admin.indices.close.CloseIndexRequest;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.admin.indices.get.GetIndexResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by quanchengyun on 2017/7/10.
 */
public class ESIndexCloseJob implements SimpleJob{
    @Autowired
    private TransportClient transportClient;

    @Value("#{'${elasticjob.prefix}'.split(',')}")
    private List<String> indexPrefix;

    @Value("${elasticjob.indexOpenDay}")
    private int indexOpenDay;

    @Override
    public void execute(ShardingContext shardingContext) {
        //do it
        //保留7天索引,之前的关闭
        //查询所有的索引
        ActionFuture<GetIndexResponse> resposne =  transportClient.admin().indices().getIndex(new GetIndexRequest());
        try {
            GetIndexResponse r= resposne.get();
            String[] indecies = r.getIndices();
            List<String> closeIndecies = new ArrayList<String>();
            for(String index: indecies){
                //获取超过7天之前的日志索引
                String prefix = null;
                if((prefix = matches(index,indexPrefix))!=null){
                    String date = index.substring(prefix.length());
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    Date d = sdf.parse(date);
                    Calendar cl = Calendar.getInstance();
                    cl.setTime(d);

                    Date now = new Date(System.currentTimeMillis());
                    Calendar lastOpenDay = Calendar.getInstance();
                    lastOpenDay.setTime(now);
                    lastOpenDay.set(Calendar.HOUR_OF_DAY,0);
                    lastOpenDay.set(Calendar.MINUTE,0);
                    lastOpenDay.set(Calendar.SECOND,0);
                    lastOpenDay.add(Calendar.DAY_OF_MONTH,-indexOpenDay-1);
                    if(cl.compareTo(lastOpenDay)<0){
                        closeIndecies.add(index);
                    }
                }
            }
            if (!closeIndecies.isEmpty()){
                transportClient.admin().indices().close(new CloseIndexRequest(closeIndecies.toArray(new String[0])));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String matches(String index , List<String> indexPrefix){
        //获取超过7天之前的日志索引
        for(String prefix: indexPrefix){
            if(index.startsWith(prefix)){
                return prefix;
            }
        }
        return null;
    }
}
