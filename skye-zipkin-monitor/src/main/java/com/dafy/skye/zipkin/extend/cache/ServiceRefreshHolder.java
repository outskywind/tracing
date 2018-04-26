package com.dafy.skye.zipkin.extend.cache;

import com.dafy.skye.zipkin.extend.dto.BasicQueryRequest;
import com.dafy.skye.zipkin.extend.service.ZipkinExtendService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by quanchengyun on 2018/4/12.
 */
@Component
public class ServiceRefreshHolder {

    private Logger log = LoggerFactory.getLogger(ServiceRefreshHolder.class);

    private ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    @Autowired
    ZipkinExtendService zipkinExtendService;

    @Value("${monitor.cache.expire}")
    long expire ;

    Set<String> servicesName = new HashSet<>();

    //不会内存泄露，无需删除过期数据
    Map<String,CacheItem<List<String>>> interfaces = new HashMap<>();

    AtomicBoolean done = new AtomicBoolean(false);

    public String[] getServicesName() {
        if(CollectionUtils.isEmpty(servicesName)){
            getServices();
        }
        return servicesName.toArray(new String[0]);
    }

    public List<String> getServiceInterfaces(String serviceName){
        CacheItem<List<String>> _cache = interfaces.get(serviceName);
        if(_cache!=null && !CollectionUtils.isEmpty(_cache.element)){
            boolean isExpire = false;
            //缓存已过期
            if(_cache.expire> 0 && _cache.expire < System.currentTimeMillis()){
                isExpire = true;
            }
            if(!isExpire){
                return _cache.element;
            }
        }
        synchronized (serviceName.intern()){
            if(done.compareAndSet(false,true)){
                List<String> element = zipkinExtendService.getServiceInterfaces(serviceName);
                interfaces.put(serviceName,new CacheItem<>(element,System.currentTimeMillis()+expire*1000));
                done.compareAndSet(true,false);
            }
        }
        return interfaces.get(serviceName).element;
    }

    @PostConstruct
    public void start(){
        executor.scheduleAtFixedRate(new Schedule(),0,5, TimeUnit.MINUTES);
    }

    public class Schedule implements Runnable{
        @Override
        public void run() {
            getServices();
        }
    }

    public void getServices(){
        BasicQueryRequest request = new BasicQueryRequest();
        request.setEndTs(System.currentTimeMillis());
        request.setLookback(7*24*3600*1000L);
        Set<String> result = zipkinExtendService.getServices(request);
        if(result!=null && !result.isEmpty()){
            log.info("refresh elasticsearch services successfully..");
            servicesName = result;
        }
    }

}
