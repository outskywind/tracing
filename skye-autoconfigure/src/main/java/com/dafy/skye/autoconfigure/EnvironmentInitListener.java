package com.dafy.skye.autoconfigure;

import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.util.ClassUtils;

import java.util.HashMap;
import java.util.Map;

public class EnvironmentInitListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    private static final String SOURCE_NAME = "skye";
    private static final String CLASS_NAME = "org.springframework.boot.actuate.autoconfigure.ManagementServerProperties.SessionCreationPolicy";

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        if(isSpringBootVersionGreaterThanOrEqualTo_1_4_2()) {
            Map<String, Object> map = initPropertiesMap();
            MapPropertySource mapPropertySource = new MapPropertySource(SOURCE_NAME, map);
            MutablePropertySources propertySources = event.getEnvironment().getPropertySources();
            propertySources.addLast(mapPropertySource);
        }
    }

    private Map<String, Object> initPropertiesMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("management.security.enabled", false);
        return map;
    }

    /**
     * @see <a href="https://github.com/spring-projects/spring-boot/issues/2142">spring boot actuator bug</a>
     * @return
     */
    private boolean isSpringBootVersionGreaterThanOrEqualTo_1_4_2() {
        return ClassUtils.isPresent(CLASS_NAME, null);
    }
}
