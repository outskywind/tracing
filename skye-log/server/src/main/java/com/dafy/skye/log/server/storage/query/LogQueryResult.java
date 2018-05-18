package com.dafy.skye.log.server.storage.query;

import com.dafy.skye.log.server.storage.entity.SkyeLogEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Caedmon on 2017/6/7.
 */
public class LogQueryResult {
    private List<SkyeLogEntity> logs = new ArrayList<>();


    public void addLogs(SkyeLogEntity skyeLogEntity){
        logs.add(skyeLogEntity);
    }

    public List<SkyeLogEntity> getLogs() {
        return logs;
    }

    public void setLogs(List<SkyeLogEntity> logs) {
        this.logs = logs;
    }

}
