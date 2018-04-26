package com.dafy.skye.log.server.collector.filter;

/**
 * Created by quanchengyun on 2018/3/30.
 */

import com.dafy.skye.log.core.logback.SkyeLogEvent;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * 注册的过滤器链
 * 过滤器链不应该保存自己的状态，
 * 要提供并发安全性
 */
public class FilterChain {
    //用普通的链表自己排序？因为数据量很小，所以手动冒泡排序就可以了
    //Set<CollectFilter> filters = new TreeSet<CollectFilter>();
    LinkedList<CollectFilter> filters = new LinkedList<>();

    public void addFilter(CollectFilter filter){
        ListIterator<CollectFilter> it = filters.listIterator();
        if(!it.hasNext()){
            filters.add(filter);
            return;
        }
        while(it.hasNext()){
            CollectFilter e =it.next();
            if(e.getOrder()>filter.getOrder() || !it.hasNext()){
                it.add(filter);
                break;
            }
        }
    }


    public Object doNextFilter(List<SkyeLogEvent> events){
        Iterator<CollectFilter> it =  this.filters.iterator();
        return it.next().filter(events,it);
    }



}
