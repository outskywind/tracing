package com.dafy.skye.server.service;

import com.dafy.skye.log.collector.storage.StorageComponent;
import com.dafy.skye.log.collector.storage.entity.SkyeLogEntity;
import com.dafy.skye.log.collector.storage.query.LogQueryRequest;
import com.dafy.skye.server.dto.SkyeLogDTO;
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
    @Override
    public List<SkyeLogDTO> queryLogs(@RequestBody LogQueryRequest request) {
        List<SkyeLogDTO> dtos=null;
        try{
            List<SkyeLogEntity> entities=storageComponent.query(request);
            if(entities==null||entities.isEmpty()){
                return new ArrayList<>(0);
            }
            dtos= new ArrayList<>(entities.size());
            for(SkyeLogEntity entity:entities){
                SkyeLogDTO.Builder builder=new SkyeLogDTO.Builder();
                builder.timestamp(entity.getTimestamp())
                        .level(entity.getLevel())
                        .traceId(entity.getTraceId())
                        .formattedMessage(formatMessage(entity));
                SkyeLogDTO dto=builder.build();
                dtos.add(dto);
            }
        }catch (Exception e){

        }

        return dtos;
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
