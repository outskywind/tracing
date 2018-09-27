package com.dafy.skye.conf;

import com.ctrip.framework.apollo.enums.PropertyChangeType;
import com.ctrip.framework.apollo.model.ConfigChange;
import com.dafy.base.conf.ConfigWrapper;
import com.dafy.base.conf.DynamicConfProperties;
import com.dafy.base.conf.DynamicConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by quanchengyun on 2018/9/25.
 */
public class SkyeDynamicConf {

    static Logger log = LoggerFactory.getLogger(SkyeDynamicConf.class);

    //key:serviceName 不是需要的serviceName是全应用唯一的
    static Map<String,ConfigWrapper> instances = new HashMap<>(4);

    private volatile static  AtomicInteger initialize= new AtomicInteger(0);

    public static final String  namespace = "IPO.skye-client";

    public static final String log_collect_key="skye.log.collect";
    public static final String report_key="skye.report";

    //if defined appName and is not defined in the apollo config server
    // it will get a null instance which is not expected
    public static ConfigWrapper getInstance(String appName) {
        //no lock with none blocking, just try it later
        try{
            while(instances.get(appName)==null){
                if(initialize.compareAndSet(0,1)){
                    DynamicConfProperties props = new DynamicConfProperties();
                    props.setAppName(appName);
                    props.setNamespace(namespace);
                    DynamicConfig delegate = new DynamicConfig(props);
                    SkyeConfigWrapper instance = new SkyeConfigWrapper(delegate,appName);
                    Set<String> keys =  new HashSet<>();
                    final String logkey  = log_collect_key+"."+appName;
                    final String reportKey  = report_key+"."+appName;
                    keys.addAll(Arrays.asList(logkey,reportKey));
                    instance.addChangeListener(changeEvent -> {
                        ConfigChange cc = changeEvent.getChange(logkey);
                        if(cc!=null){
                            switch (cc.getChangeType()){
                                case MODIFIED:
                                     instance.setProperty(log_collect_key,!"false".equalsIgnoreCase(cc.getNewValue()));
                                case ADDED:
                                     instance.setProperty(log_collect_key,!"false".equalsIgnoreCase(cc.getNewValue()));
                                case DELETED:
                                     instance.setProperty(log_collect_key,null);
                            }
                        }
                        cc = changeEvent.getChange(reportKey);
                        if(cc!=null){
                            switch (cc.getChangeType()){
                                case MODIFIED:
                                    instance.setProperty(report_key,!"false".equalsIgnoreCase(cc.getNewValue()));
                                case ADDED:
                                    instance.setProperty(report_key,!"false".equalsIgnoreCase(cc.getNewValue()));
                                case DELETED:
                                    instance.setProperty(report_key,null);
                            }
                        }
                    },keys);
                    instances.put(appName,instance);
                    initialize.compareAndSet(1,2);
                }
                if(initialize.get()==2){
                    break;
                }
            }
        }catch (Throwable ex){
            log.warn("initialize dynamicConf failed ",ex);
        }
        return instances.get(appName);
    }


}
