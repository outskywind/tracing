package com.dafy.skye.zk;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;

/**
 * Created by quanchengyun on 2017/9/18.
 */
public class ZkListener implements PathChildrenCacheListener {

    private ZkSerivceDiscovery zkSerivceDiscovery;

    @Override
    public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent event) throws Exception {
        PathChildrenCacheEvent.Type eventType = event.getType();
        switch (eventType) {
            case CHILD_ADDED:
                zkSerivceDiscovery.refresh();
                break;
            case CHILD_UPDATED:
                zkSerivceDiscovery.refresh();
                break;
            case CHILD_REMOVED:
                zkSerivceDiscovery.refresh();
                break;
            case CONNECTION_RECONNECTED:
                zkSerivceDiscovery.refresh();
                break;
            case CONNECTION_SUSPENDED:break;
            case CONNECTION_LOST:
                System.out.println("Connection error,waiting...");
                break;
            default:
                System.out.println("Data:" + event.getData().toString());
        }
    }


    public void setZkSerivceDiscovery(ZkSerivceDiscovery zkSerivceDiscovery) {
        this.zkSerivceDiscovery = zkSerivceDiscovery;
        zkSerivceDiscovery.getZkCache().getListenable().addListener(this);
    }
}
