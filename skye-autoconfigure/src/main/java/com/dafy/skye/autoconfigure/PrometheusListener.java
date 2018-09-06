package com.dafy.skye.autoconfigure;

import com.dafy.skye.log.core.SkyeLogUtil;
import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.agent.model.NewService;
import io.prometheus.client.hotspot.DefaultExports;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.embedded.EmbeddedServletContainerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;

import java.util.Collections;

public class PrometheusListener implements ApplicationListener<EmbeddedServletContainerInitializedEvent> {

    private static final Logger logger = LoggerFactory.getLogger(PrometheusListener.class);

    private static final String TAG = "app-service";

    @Override
    public void onApplicationEvent(EmbeddedServletContainerInitializedEvent event) {
        try {
            Environment env = event.getApplicationContext().getEnvironment();
            String consulAddresses = env.getProperty("skye.consulServer");
            if(StringUtils.isEmpty(consulAddresses) || !consulAddresses.contains(":")) {
                logger.error("skye.consulServer is invalid! skye.consulServer={}", consulAddresses);
                return;
            }

            String serviceName = env.getProperty("skye.serviceName");
            String checkInterval = env.getProperty("skye.consulCheckInterval");
            String appHost = SkyeLogUtil.getPrivateIp();
            int appPort = event.getEmbeddedServletContainer().getPort();
            String serviceId = serviceName + "-" + appHost + "-" + appPort;
            String tcpAddr = appHost + ':' + appPort;

            NewService newService = new NewService();
            newService.setId(serviceId);
            newService.setName(serviceName);
            newService.setAddress(appHost);
            newService.setPort(appPort);
            newService.setTags(Collections.singletonList(TAG));

            NewService.Check check = new NewService.Check();
            check.setTcp(tcpAddr);
            check.setInterval(checkInterval);

            newService.setCheck(check);

            for(String addr : consulAddresses.split(",")) {
                String[] addrArr = addr.trim().split(":");
                String consulHost = addrArr[0];
                int consulPort = addrArr.length == 1 ? 80 : Integer.parseInt(addrArr[1]);

                ConsulClient client = new ConsulClient(consulHost, consulPort);
                client.agentServiceRegister(newService);
                logger.info("register service to consul successfully! serviceName={}, consulHost={}, consulPort={}, appHost={}, appPort={}",
                        serviceName, consulHost, consulPort, appHost, appPort);
            }

            DefaultExports.initialize();
            logger.info("prometheus exports initialized!");
        } catch (Throwable e) {
            logger.error("PrometheusListener onApplicationEvent error!", e);
        }
    }
}
