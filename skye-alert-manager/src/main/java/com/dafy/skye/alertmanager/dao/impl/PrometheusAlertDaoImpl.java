package com.dafy.skye.alertmanager.dao.impl;

import com.dafy.skye.alertmanager.dao.PrometheusAlertDao;
import com.dafy.skye.alertmanager.po.PrometheusAlertPO;
import com.dafy.skye.alertmanager.mapper.PrometheusAlertMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PrometheusAlertDaoImpl implements PrometheusAlertDao {

    private final PrometheusAlertMapper mapper;

    @Autowired
    public PrometheusAlertDaoImpl(PrometheusAlertMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public int addAlert(PrometheusAlertPO alert) {
        return mapper.addAlert(alert);
    }

    @Override
    public List<PrometheusAlertPO> getAlerts(String[] services, String startTime, String endTime, int limit) {
        return mapper.getAlerts(startTime, endTime, limit);
    }

    @Override
    public List<PrometheusAlertPO> getAlertsByIds(long[] ids) {
        return mapper.getAlertsByIds(ids);
    }

    @Override
    public PrometheusAlertPO getAlertById(long id) {
        return mapper.getAlertById(id);
    }
}
