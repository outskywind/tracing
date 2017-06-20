package com.dafy.skye.log.server.storage.query;

import com.dafy.skye.common.query.QueryResult;
import com.dafy.skye.log.server.storage.entity.SkyeLogEntity;

import java.util.List;
import java.util.Map;

/**
 * Created by Caedmon on 2017/6/7.
 */
public class LogQueryResult extends QueryResult{
    private List<SkyeLogEntity> content;
    private Integer total;

    public List<SkyeLogEntity> getContent() {
        return content;
    }

    public void setContent(List<SkyeLogEntity> content) {
        this.content = content;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    @Override
    public String toString() {
        return "LogQueryResult{" +
                "took=" + took +
                ", success=" + success +
                ", error='" + error + '\'' +
                ", extra=" + extra +
                ", content=" + content +
                ", total=" + total +
                '}';
    }
}
