package com.dafy.skye.server.service;

import com.dafy.skye.log.collector.storage.StorageComponent;
import com.dafy.skye.log.collector.storage.entity.SkyeLogEntity;
import com.dafy.skye.log.collector.storage.query.LogQueryRequest;
import com.dafy.skye.log.collector.storage.query.LogQueryResult;
import com.dafy.skye.server.dto.SkyeLogQueryResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Caedmon on 2017/6/5.
 * TODO 搜索记录要记录下来
 */
@Service
public class SkyeServiceImpl implements SkyeService{
    @Autowired
    private StorageComponent storageComponent;
    private static final long DEFAULT_LOOKBACK=24*3600*1000;
    @Override
    public LogQueryResult logsQuery(LogQueryRequest request) {
        if(request.getStartTs()==null){
            request.setStartTs(System.currentTimeMillis()-DEFAULT_LOOKBACK);
        }
        if(request.getEndTs()==null){
            request.setEndTs(System.currentTimeMillis());
        }
        return storageComponent.query(request);
    }
    public static String formatMessage(SkyeLogEntity entity){
        StringBuilder builder=new StringBuilder();
        builder.append("[").append(entity.getPid()).append("@")
                .append(entity.getThread()).append("]")
                .append(" [").append(entity.getMdc())
                .append("] ").append(entity.getMessage());
        return builder.toString();
    }
}
