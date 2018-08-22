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

public class PrometheusListener implements ApplicationListener<EmbeddedServletContainerInitializedEvent> {

    private static final Logger logger = LoggerFactory.getLogger(PrometheusListener.class);

    @Override
    public void onApplicationEvent(EmbeddedServletContainerInitializedEvent event) {
        try {
            Environment env = event.getApplicationContext().getEnvironment();
            String consulAddress = env.getProperty("skye.consulServer");
            if(StringUtils.isEmpty(consulAddress) || !consulAddress.contains(":")) {
                logger.error("skye.consulServer is invalid! skye.consulServer={}", consulAddress);
                return;
            }

            String[] consulAddressArr = consulAddress.split(":");
            String consulHost = consulAddressArr[0];
            int consulPort = Integer.parseInt(consulAddressArr[1]);

            String appHost = SkyeLogUtil.getPrivateIp();
            int appPort = event.getEmbeddedServletContainer().getPort();
            String serviceName = env.getProperty("skye.serviceName");
            String checkInterval = env.getProperty("skye.consulCheckInterval");

            ConsulClient client = new ConsulClient(consulHost, consulPort);
            NewService newService = new NewService();
            newService.setName(serviceName);
            newService.setAddress(appHost);
            newService.setPort(appPort);

            NewService.Check check = new NewService.Check();
            check.setTcp(appHost + ':' + appPort);
            check.setInterval(checkInterval);

            newService.setCheck(check);

            client.agentServiceRegister(newService);
            logger.info("register service to consul successfully! serviceName={}, consulHost={}, consulPort={}, appHost={}, appPort={}",
                    serviceName, consulHost, appHost, appPort);

            DefaultExports.initialize();
            logger.info("prometheus exports initialized!");
        } catch (Exception e) {
            logger.error("PrometheusListener onApplicationEvent error!", e);
        }
    }
}
