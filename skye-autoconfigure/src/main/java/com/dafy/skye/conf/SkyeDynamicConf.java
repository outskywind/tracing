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

    static Map<String,ConfigWrapper> instances = new HashMap<>(32);

    private volatile static  AtomicInteger initialize= new AtomicInteger(0);

    public static final String  namespace = "IPO.skye-client";

    public static final String log_collect_key="skye.log.collect";
    public static final String report_key="skye.report";

    public static ConfigWrapper getInstance(String serviceName) {
        //no lock with none blocking, just try it later
        try{
            while(instances.get(serviceName)==null){
                if(initialize.compareAndSet(0,1)){
                    DynamicConfProperties props = new DynamicConfProperties();
                    props.setNamespace(namespace);
                    DynamicConfig delegate = new DynamicConfig(props);
                    ConfigWrapper instance = new ConfigWrapper(delegate);
                    Set<String> keys =  new HashSet<>();
                    keys.addAll(Arrays.asList(log_collect_key,report_key));
                    final String logkey  = log_collect_key+"."+serviceName;
                    final String reportKey  = report_key+"."+serviceName;
                    instance.addChangeListener(changeEvent -> {
                        ConfigChange cc = changeEvent.getChange(logkey);
                        if(cc!=null && cc.getChangeType()== PropertyChangeType.MODIFIED){
                            instance.addProperty(log_collect_key,!"false".equalsIgnoreCase(cc.getNewValue()));
                        }
                        cc = changeEvent.getChange(reportKey);
                        if(cc!=null && cc.getChangeType()== PropertyChangeType.MODIFIED){
                            instance.addProperty(report_key,!"false".equalsIgnoreCase(cc.getNewValue()));
                        }
                    },keys);
                    instances.put(serviceName,instance);
                    initialize.compareAndSet(1,2);
                }
                if(initialize.get()==2){
                    break;
                }
            }
        }catch (Throwable ex){
            log.warn("initialize dynamicConf failed ",ex);
        }
        return instances.get(serviceName);
    }




}
