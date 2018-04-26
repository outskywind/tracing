package com.dafy.skye.log.server.collector.filter;

import ch.qos.logback.classic.Level;
import com.dafy.skye.log.core.logback.SkyeLogEvent;

import java.util.Iterator;
import java.util.List;

/**
 * Created by quanchengyun on 2018/4/3.
 */
public class LogLevelFilter implements CollectFilter {

    private int order ;
    @Override
    public Object filter(List<SkyeLogEvent> events, Iterator<CollectFilter> filters) {
        Iterator<SkyeLogEvent> it = events.iterator();
        while(it.hasNext()){
            SkyeLogEvent event = it.next();
            if(event.getLevel()== Level.DEBUG){
                it.remove();
            }
        }
        return filters.next().filter(events,filters);
    }

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    public void setOrder(int order) {
        this.order=order;
    }


}
