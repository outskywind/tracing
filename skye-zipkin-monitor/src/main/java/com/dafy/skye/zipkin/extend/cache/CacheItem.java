package com.dafy.skye.zipkin.extend.cache;


/**
 * Created by quanchengyun on 2018/4/26.
 */
public class CacheItem<T> {

    public T element;
    public long expire=0L;

    public CacheItem(){
    }

    public CacheItem(T element,long expire){
        this.element = element;
        this.expire = expire;
    }


}
