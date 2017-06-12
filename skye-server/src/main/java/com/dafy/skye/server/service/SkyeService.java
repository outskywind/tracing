package com.dafy.skye.server.service;

import com.dafy.skye.log.collector.storage.query.LogQueryRequest;
import com.dafy.skye.log.collector.storage.query.LogQueryResult;
import com.dafy.skye.server.dto.SkyeLogQueryResult;

import java.util.List;

/**
 * Created by Caedmon on 2017/6/5.
 */
public interface SkyeService {

    LogQueryResult logsQuery(LogQueryRequest request);
}
