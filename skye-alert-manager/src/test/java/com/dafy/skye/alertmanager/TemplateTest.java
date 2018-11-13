package com.dafy.skye.alertmanager;

import com.dafy.skye.alertmanager.po.PrometheusAlertPO;
import com.dafy.skye.alertmanager.util.TemplateUtil;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TemplateTest {

    private static final String FTL_ALERT = "alert.ftl";

    @Test
    public void testRenderTemplate() {
        PrometheusAlertPO p1 = new PrometheusAlertPO();
        p1.setSummary("测试test1111111");

        PrometheusAlertPO p2 = new PrometheusAlertPO();
        p2.setSummary("测试test22222222");
        p2.setGrafanaLink("http://www.baidu.com");

        List<PrometheusAlertPO> alerts = new ArrayList<>();
        alerts.add(p1);
        alerts.add(p2);


        Map<String, Object> data = new HashMap<>();
        data.put("alerts", alerts);

        String content = TemplateUtil.renderTemplate(FTL_ALERT, data);
        System.out.println(content);
    }

}
