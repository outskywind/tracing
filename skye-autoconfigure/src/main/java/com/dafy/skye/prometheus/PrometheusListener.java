package com.dafy.skye.prometheus;

import com.dafy.skye.util.ClientVersionUtil;
import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.agent.model.NewService;
import com.google.common.base.Joiner;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class PrometheusListener implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger logger = LoggerFactory.getLogger(PrometheusListener.class);

    private static final String TAG = "app-service";
    private static final String CONTEXT_PATH = "/prometheus";
    private static final int DEFAULT_PORT = 12431;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        try {
            Environment env = event.getApplicationContext().getEnvironment();
            String portStr = env.getProperty("skye.prometheus.port");
            int appPort = StringUtils.isEmpty(portStr) ? DEFAULT_PORT : Integer.parseInt(portStr);
            startHttpServer(appPort, CONTEXT_PATH);

            String consulAddresses = env.getProperty("skye.consulServer");
            if(StringUtils.isEmpty(consulAddresses)) {
                logger.warn("skye.consulServer is invalid! skye.consulServer={}", consulAddresses);
                return;
            }
            String appName = env.getProperty("appName");
            String serviceName = StringUtils.isNotBlank(env.getProperty("skye.serviceName"))?
                    env.getProperty("skye.serviceName"):env.getProperty("skye.service-name") ;
            if(StringUtils.isNotBlank(appName)){
                serviceName = appName;
            }
            String checkInterval = StringUtils.isNotBlank(env.getProperty("skye.consulCheckInterval"))?
                    env.getProperty("skye.consulCheckInterval"):env.getProperty("skye.consul-check-interval");
            String appHost = InetAddress.getLocalHost().getHostAddress();
            String hostName = getHostName();
            String serviceId = Joiner.on('-').join(serviceName, hostName);
            String tcpAddr = Joiner.on(':').join(appHost, appPort);

            Map<String, String> meta = new HashMap<>();
            meta.put("hostname", hostName);
            //key 不能包含 '.'
            meta.put("skye-version", ClientVersionUtil.version());

            NewService newService = new NewService();
            newService.setId(serviceId);
            newService.setName(serviceName);
            newService.setAddress(appHost);
            newService.setPort(appPort);
            newService.setTags(Collections.singletonList(TAG));
            newService.setMeta(meta);


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
            SkyeExports.initialize();
            logger.info("prometheus exports initialized!");
        } catch (Throwable e) {
            logger.warn("PrometheusListener onApplicationEvent error!", e);
        }
    }

    private String getHostName() throws Exception {
        try(
            InputStream is = Runtime.getRuntime().exec("hostname").getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is))
        ) {
            return reader.readLine();
        }
    }

    private void startHttpServer(int port, String contextPath) {
        Thread t = new Thread(()->{
            try{
                new PrometheusServer(port, contextPath);
                logger.info("prometheus server start successfully");
            }catch (Throwable ex){
                logger.warn("prometheus server start failed" , ex);
            }
        });
        t.start();
    }
}
