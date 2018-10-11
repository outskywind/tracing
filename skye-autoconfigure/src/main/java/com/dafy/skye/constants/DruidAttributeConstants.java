package com.dafy.skye.constants;

/**
 * 阿里数据库连接池druid属性常量
 *
 * @see <a href="http://120.26.192.168/druid/datasource.html">Druid Monitor在线示例</a>
 * @see <a href="https://github.com/alibaba/druid/wiki/DruidDataSource%E9%85%8D%E7%BD%AE%E5%B1%9E%E6%80%A7%E5%88%97%E8%A1%A8">DruidDataSource配置属性列表</a>
 */
public enum DruidAttributeConstants {

    MAX_ACTIVE("MaxActive", "alibaba_druid_max_active", "最大连接数"),
    MIN_IDLE("MinIdle", "alibaba_druid_min_idle", "最小空闲连接数"),
    NOT_EMPTY_WAIT_MILLIS("NotEmptyWaitMillis", "alibaba_druid_not_empty_wait_millis", "获取连接时最多等待多长时间"),
    POOLING_COUNT("PoolingCount", "alibaba_druid_pooling_count", "当前连接池中的连接数"),
    POOLING_PEAK("PoolingPeak", "alibaba_druid_pooling_peak", "连接池中连接数的峰值"),
    ACTIVE_COUNT("ActiveCount", "alibaba_druid_active_count", "当前连接池中活跃连接数"),
    ACTIVE_PEAK("ActivePeak", "alibaba_druid_active_peak", "连接池中活跃连接数峰值");

    private final String jmxName;
    private final String exportedName;
    private final String desc;

    DruidAttributeConstants(String jmxName, String exportedName, String desc) {
        this.jmxName = jmxName;
        this.exportedName = exportedName;
        this.desc = desc;
    }

    public String getJmxName() {
        return jmxName;
    }

    public String getExportedName() {
        return exportedName;
    }

    public String getDesc() {
        return desc;
    }
}
