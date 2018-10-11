package com.dafy.skye.prometheus;

import com.dafy.skye.constants.DruidAttributeConstants;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.prometheus.client.Collector;
import io.prometheus.client.GaugeMetricFamily;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.*;
import java.lang.management.ManagementFactory;
import java.util.*;

import static com.dafy.skye.constants.DruidAttributeConstants.*;

/**
 * 阿里数据库连接池Druid监控数据收集器
 */
public class DruidExports extends Collector {

    private static final Logger logger = LoggerFactory.getLogger(DruidExports.class);

    private static final String NAME_PATTERN_DATASOURCE = "com.alibaba.druid.pool:type=DruidDataSource,*";

    private static final String NAME_STAT_SERVICE = "com.alibaba.druid:type=DruidStatService";

    // orderBy      排序字段，TotalTime：SQL执行时间，ExecuteCount：SQL执行次数
    // orderType    排序顺序，asc：降序，desc：升序【注意：这里值的意思和实际结果是相反的。。。】
    // page         页数
    // perPageCount 每页SQL数量
    private static final String URL_QUERY_SLOW_SQL = "/sql.json?orderBy=TotalTime&orderType=asc&page=1&perPageCount=10";

    private final MBeanServer mbeanServer;

    // 一个工程中可以有多个druid数据源
    private Set<ObjectName> dataSourceNameSet;

    private ObjectName statServiceName;

    // Prometheus指标中label名列表
    private static final List<String> DATADOURCE_LABELS = Collections.singletonList("ds_name");

    private static final List<String> SQL_LABELS = Arrays.asList("sql", "fetch_row_count", "max_timespan_occur_time");

    private static final JsonParser PARSER = new JsonParser();

    public DruidExports() {
        this(ManagementFactory.getPlatformMBeanServer());
    }

    public DruidExports(MBeanServer mbeanServer) {
        this.mbeanServer = mbeanServer;
        checkIfDruidDataSourceMBeanRegistered();
        checkIfDruidStatServiceMBeanRegistered();
    }

    private void checkIfDruidDataSourceMBeanRegistered() {
        try {
            Set<ObjectName> objectNameSet = this.mbeanServer.queryNames(new ObjectName(NAME_PATTERN_DATASOURCE), null);
            if(objectNameSet.isEmpty()) {
                throw new UnsupportedOperationException();
            }

            this.dataSourceNameSet = objectNameSet;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void checkIfDruidStatServiceMBeanRegistered() {
        try {
            ObjectName name = new ObjectName(NAME_STAT_SERVICE);
            if(this.mbeanServer.isRegistered(name)) {
                this.statServiceName = name;
            } else {
                logger.warn("MBean DruidStatService fail to be registered! Please add 'stat' to configuration 'spring.datasource.filters'!");
            }
        } catch (Exception e) {
            logger.error("fail to check DruidStatService", e);
        }
    }

    @Override
    public List<MetricFamilySamples> collect() {
        List<MetricFamilySamples> mfs = new LinkedList<>();

        exportJmxAttr(mfs, MAX_ACTIVE);
        exportJmxAttr(mfs, MIN_IDLE);
        exportJmxAttr(mfs, NOT_EMPTY_WAIT_MILLIS);
        exportJmxAttr(mfs, POOLING_COUNT);
        exportJmxAttr(mfs, POOLING_PEAK);
        exportJmxAttr(mfs, ACTIVE_COUNT);
        exportJmxAttr(mfs, ACTIVE_PEAK);

        exportSlowSql(mfs);

        return mfs;
    }

    private void exportJmxAttr(List<MetricFamilySamples> mfs, DruidAttributeConstants attrConstants) {
        try {
            GaugeMetricFamily metricFamily = new GaugeMetricFamily(attrConstants.getExportedName(), attrConstants.getDesc(), DATADOURCE_LABELS);
            for(ObjectName dataSourceName : this.dataSourceNameSet) {
                String dsName = getNameOfDataSourceBean(dataSourceName);
                double attrVal = getJmxAttributeValue(dataSourceName, attrConstants);
                metricFamily.addMetric(Collections.singletonList(dsName), attrVal);
                mfs.add(metricFamily);
            }
        } catch (Exception e) {
            logger.error("Could not access JMX attribute '{}'!", attrConstants.getJmxName(), e);
        }
    }

    private String getNameOfDataSourceBean(ObjectName dataSourceName) {
        String dsName = dataSourceName.getKeyProperty("name");
        return dsName == null ? "" : dsName;
    }

    private double getJmxAttributeValue(ObjectName dataSourceName, DruidAttributeConstants attrConstants) throws Exception {
        Object attrValObj = this.mbeanServer.getAttribute(dataSourceName, attrConstants.getJmxName());
        double attrVal;
        if(attrValObj.getClass() == Integer.class || attrValObj.getClass() == int.class) {
            attrVal = (int) attrValObj;
        } else if(attrValObj.getClass() == Long.class || attrValObj.getClass() == long.class) {
            attrVal = (long) attrValObj;
        } else {
            attrVal = (double) attrValObj;
        }
        return attrVal;
    }

    // 不以数据源为维度来进行统计
    private void exportSlowSql(List<MetricFamilySamples> mfs) {
        if(this.statServiceName != null) {
            try {
                String result = getTop10ForSlowSqlInJson();

                JsonElement rootNode = PARSER.parse(result);
                JsonObject rootObj = rootNode.getAsJsonObject();
                int resultCode = rootObj.getAsJsonPrimitive("ResultCode").getAsInt();
                // resultCode为1表示请求成功
                if(resultCode == 1) {
                    JsonElement contentElem = rootObj.get("Content");
                    if(contentElem == null || contentElem.isJsonNull()) {
                        return;
                    }
                    JsonArray contentArray = (JsonArray) contentElem;
                    JsonObject contentObj;

                    // executeCount：同一条SQL（可以包含占位符）被执行的总次数
                    // totalTime：同一条SQL（可以包含占位符）累计执行的总时间
                    // fetchRowCount：执行该SQL的过程中读取的表记录数
                    long executeCount, totalTime, fetchRowCount;

                    // sql：SQL语句
                    // maxTimespanOccurTime：SQL执行的起始时间
                    String sql, maxTimespanOccurTime;

                    for(JsonElement elem : contentArray) {
                        contentObj = elem.getAsJsonObject();
                        totalTime = contentObj.getAsJsonPrimitive("TotalTime").getAsLong();
                        // 由于druid存储sql的时候是做了sql格式化的，这里为了方便显示取消了格式化
                        sql = contentObj.getAsJsonPrimitive("SQL").getAsString().replaceAll("\\s{2,}", " ");
                        fetchRowCount = contentObj.getAsJsonPrimitive("FetchRowCount").getAsLong();
                        executeCount = contentObj.getAsJsonPrimitive("ExecuteCount").getAsLong();
                        maxTimespanOccurTime = contentObj.getAsJsonPrimitive("MaxTimespanOccurTime").getAsString();

                        // 这里保存sql的平均执行时间
                        GaugeMetricFamily metricFamily = new GaugeMetricFamily("alibaba_druid_slow_sql", "慢SQL", SQL_LABELS);
                        metricFamily.addMetric(Arrays.asList(sql, String.valueOf(fetchRowCount), maxTimespanOccurTime),
                                totalTime / executeCount);

                        mfs.add(metricFamily);
                    }
                }
            } catch (Exception e) {
                logger.error("fail to get slow sql!", e);
            }
        }
    }

    private String getTop10ForSlowSqlInJson() throws Exception {
        return (String) this.mbeanServer.invoke(this.statServiceName, "service",
                new String[] { URL_QUERY_SLOW_SQL },
                new String[] { String.class.getName() });
    }
}
