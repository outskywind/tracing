package com.dafy.skye.zipkin.extend.cache;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by quanchengyun on 2018/5/3.
 */

/**
 * a in-mem cache implements in map which space is unlimited.
 * be aware of memory leak
 */
public class Cache {

    Map<String,CacheItem<?>> holder = new HashMap<>();

    Set<String> FIFOkeys =new HashSet<>();

    public <T> T get(String key){
        CacheItem<?> item = holder.get(key);
        if(item!=null && (item.expire<=0 || item.expire < System.currentTimeMillis())){
            return (T)item.element;
        }
        return null;
    }
    /**
     *
     * @param key
     * @param value
     * @param expireSecs  有效期 单位s
     * @return if value is null then return false
     */
    public  boolean add(String key, Object value , long expireSecs){
        if(value==null){
            return false;
        }
        CacheItem<?> item = null;
        if(expireSecs<=0){
             item = new CacheItem<>(value);
        }
        else{
             item = new CacheItem<>(value,System.currentTimeMillis()+expireSecs*1000);
        }
        holder.put(key,item);
        return true;
    }

    public  boolean add(String key, Object value){
        return add(key,value,0);
    }


    public void remove(String key){
        holder.remove(key);
    }

    public void clear(){
        holder.clear();
    }




}
