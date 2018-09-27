package com.base;

import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.common.utils.Utils;
import org.junit.Test;

/**
 * Created by quanchengyun on 2018/9/26.
 */
public class TestHash {


    public String[] ips= {"10.40.11.80","10.40.11.81","10.40.11.82","10.40.11.83","10.40.11.84","10.40.11.85"};
    public String[] serviceNames = {"capital-kaola-server","capital-qgz-server","channel-bus-provider","channel-bus-server","paymentcenter","platform-account-provider","platform-aidaccountcenter","platform-aidcenter-rpc","platform-credit-rpc","platform-loanrepay-rpc","platform-sevend-server","third-party-data-server"};

    public int[] pids = {42422,1,12345,999, 10023,56777,3211,4098,33321,84343,1111,4653454,222,56565,14022};

    Serializer<String> keySerializer=new StringSerializer();

    public void doTest(){

        int numPartitions = 6;

        String topic = "skye-log";
        int[] count = new int[]{0,0,0,0,0,0};
        int keynum1 = 1;
        int total = keynum1* ips.length * pids.length;
        for(int i=0;i<keynum1;i++){
            for(int j=0;j<ips.length;j++){
                for(int k=0;k<pids.length;k++){
                    String key = serviceNames[i]+ips[j]+pids[k];
                    byte[] serializedKey = keySerializer.serialize(topic, key);
                    int partition = Utils.toPositive(Utils.murmur2(serializedKey)) % numPartitions;
                    count[partition]++;
                }
            }
        }
        System.out.println("唯一hashkey数为： "+total+"，topic的分区数为："+numPartitions);
        for(int i=0;i<count.length;i++){
            System.out.println("分区 "+i+" hash命中数："+count[i] + " 命中率%为："+ (count[i]*100/total)+"%");
        }
    }

    @Test
    public void testRound(){
        int round = 1;
        for(int i=0;i<round;i++){
            doTest();
        }

    }









}
