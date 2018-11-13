package com.dafy.skye.alertmanager.dao.impl;

import com.dafy.skye.alertmanager.constant.NotificationChannel;
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
    public boolean addAlert(PrometheusAlertPO alert) {
        int count = mapper.addAlert(alert);
        return count > 0;
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

    @Override
    public boolean updateAlerts(long[] ids, String receivers, String cc, NotificationChannel notificationChannel) {
        int count = mapper.updateAlerts(ids, receivers, cc, notificationChannel.getValue());
        return count > 0;
    }
}
