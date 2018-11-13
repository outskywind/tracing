package com.dafy.skye.alertmanager.mapper;

import com.dafy.skye.alertmanager.po.PrometheusAlertPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface PrometheusAlertMapper {

    int addAlert(PrometheusAlertPO alert);

    List<PrometheusAlertPO> getAlerts(@Param("startTime") String startTime,
                                      @Param("endTime") String endTime,
                                      @Param("limit") int limit);

    List<PrometheusAlertPO> getAlertsByIds(@Param("ids") long[] ids);

    PrometheusAlertPO getAlertById(@Param("id") long id);
}
