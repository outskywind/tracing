package com.dafy.skye.zipkin;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 参考 snowflake
 * 41bit timestamp + 10bit <machine ID>+12bit
 * Created by quanchengyun on 2018/10/16.
 */
public class IDGenerator {

    //目前时间戳最高有效位40位,2018-10-16 00:00:00.000
    private static  final long  START = 1539619200000L;
    private static final int MOV=64-42;
    private static final long MASK = ((1L<<41)-1);//shit 1 is parsed as int

    private static volatile AtomicInteger state = new AtomicInteger(0);
    private static long  workerID=0;
    //序列号器，全局共享，线程竞争，循环自增
    private static volatile AtomicInteger seqNo = new AtomicInteger(0);

    public static long getId(){
        //取低41位
        long timestap = System.currentTimeMillis()-START;
        long new41bit = (timestap & MASK);
        //
        int v = (seqNo.getAndIncrement()&((1<<12)-1));
        return (new41bit<<MOV)+(workerId()<<12)+v;
    }

    //10bit  2^10=1024
    //子网+pid , 子网128=7bit,
    private static long  workerId(){
        while(workerID==0){
            //只有一个初始化
            if(state.compareAndSet(0,1)){
                //will we just register on the workerId
                long networkIp = networkIp();
                long processId = processId();
                workerID = ((networkIp<<3) & processId);
                state.compareAndSet(1,2);
            }
        }
        return workerID;
    }

    private static long processId() {
        // Note: may fail in some JVM implementations
        // therefore fallback has to be provided

        // something like '<pid>@<hostname>', at least in SUN / Oracle JVMs
        final String jvmName = ManagementFactory.getRuntimeMXBean().getName();
        final int index = jvmName.indexOf('@');
        try {
            if (index >= 1){
                return Long.parseLong(jvmName.substring(0, index));
            }
        } catch (NumberFormatException e) {
            // ignore
        }
        return new Random().nextInt(7);
    }

    //ip段可以使用永久节点
    private static long  networkIp(){
        long networkIp=0;
        try{
            byte[] ip= InetAddress.getLocalHost().getAddress();
            byte b = ip[ip.length-1];
            //低7位
            networkIp = b & ((1<<7)-1);
        }catch (Throwable tx){
            //随机
            networkIp = new Random().nextInt(127);
        }
        return networkIp;
    }


    public static long extractTimestamp(long id){
        return ((id >>> MOV) & MASK) + START;
    }


    public static void main(String[] args){
        try{
            long id  = IDGenerator.getId();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            System.out.println(sdf.format(new Date(IDGenerator.extractTimestamp(id))));
        }catch (Throwable tx){
            tx.printStackTrace();
        }
    }








}
