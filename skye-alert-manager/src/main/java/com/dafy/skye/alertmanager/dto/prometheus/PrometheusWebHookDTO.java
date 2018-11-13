package com.dafy.skye.alertmanager.dto.prometheus;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

/**
 * @see <a href="https://prometheus.io/docs/alerting/configuration/#webhook_config">webhook配置</a>
 * @see <a href="https://prometheus.io/docs/alerting/notifications">告警字段说明</a>
 */
@Setter
@Getter
@ToString
public class PrometheusWebHookDTO {

    /**
     * 数据格式的版本号，目前是4
     */
    private String version;

    /**
     * 对应alertmanager.yml中的<code>route.match_re</code>
     */
    private String groupKey;

    /**
     * 有<b>resolved</b>、<b>firing</b>这两种状态
     */
    private String status;

    /**
     * 对应alertmanager.yml中的receivers.name。目前我们统一使用webhook这个名称。
     */
    private String receiver;

    /**
     * 目前没用到
     */
    private Map<String, String> groupLabels;

    /**
     * 告警分组中公共的标签。目前有如下标签：
     * <ul>
     *     <li>hostname</li>
     *     <li>instance</li>
     *     <li>job</li>
     *     <li>service</li>
     *     <li>warning_level</li>
     *     <li>warning_service</li>
     * </ul>
     */
    private Map<String, String> commonLabels;

    /**
     * 目前没用到
     */
    private Object commonAnnotations;

    /**
     * 访问alertmanager组件的URL（其中ip部分可能会使用主机名代替）
     */
    private String externalURL;

    /**
     * 告警列表
     */
    private Alert[] alerts;

    @Setter
    @Getter
    @ToString
    public static class Alert {
        /**
         * 有<b>resolved</b>、<b>firing</b>这两种状态
         */
        private String status;

        /**
         * 比<code>commonLabels</code>多了如下标签：
         * <ul>
         *     <li>alertname</li>
         *     <li>receiver_group</li>
         *     <li>其它自定义标签</li>
         * </ul>
         */
        private Map<String, String> labels;

        /**
         * 对应规则文件中的<code>groups.annotations</code>
         */
        private Map<String, String> annotations;

        /**
         * 当前告警状态转为firing时的时间。格式：
         * <code>2018-11-05T14:12:43.896932934+08:00</code>
         */
        private String startsAt;

        /**
         * 通常和<code>startsAt</code>字段值相同
         */
        private String endsAt;

        /**
         * 访问Prometheus的URL，其中携带了定义在规则文件中的<code>groups.expr</code>。
         * 例子：<code>http://hn-kafka01-184-180.7daichina.com:9080/graph?g0.expr=sql_kaola_loan_failure_rate+>=+0.05&g0.tab=1</code>
         */
        private String generatorURL;
    }
}
