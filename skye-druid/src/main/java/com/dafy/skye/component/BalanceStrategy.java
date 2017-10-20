package com.dafy.skye.component;

/**
 * Created by quanchengyun on 2017/9/19.
 */

import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 负载均衡策略
 * 通常有1.一致hash,这个是根据指定一个key来
 * 2.RR  3.最少连接数 4. 最快响应时间 5.最小负载<需要server端额外数据反馈>
 */
public enum BalanceStrategy {

    ROUND_ROBIN(RoundRobin.INSTANCE), CONST_HASH(null), LEAST_CONNECT(null),LEAST_RESP(null),LEAST_LOAD(null);

    public Strategy strategy;

    BalanceStrategy(Strategy strategy){
        this.strategy=strategy;
    }


    public static class RoundRobin implements Strategy{

        public static final RoundRobin INSTANCE =new  RoundRobin();

        public static final AtomicLong rr_counter=new AtomicLong(0);
        @Override
        public ServerInfo chooseNext(Collection<ServerInfo> candidates) {
            long nextIndex = rr_counter.incrementAndGet();
            int candidatesIndex =  (int)(nextIndex/candidates.size());
            return (ServerInfo) candidates.toArray()[candidatesIndex];
        }
    }
}
