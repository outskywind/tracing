package com.dafy.skye.klog.collector;

import ch.qos.logback.classic.PatternLayout;
import com.dafy.skye.klog.core.logback.KLogConverter;

import java.io.InputStream;
import java.util.Properties;

/**
 * Created by Caedmon on 2017/4/14.
 */
public class CollectorStartup {

    public static void main(String[] args) throws Exception{
        String propConfig="skye-klog-collector.properties";
        Properties props=new Properties();
        InputStream in = CollectorStartup.class.getClassLoader()
                .getResourceAsStream(propConfig);
        props.load(in);
        CollectorController controller=new CollectorController(props);
        controller.start();
    }
}
