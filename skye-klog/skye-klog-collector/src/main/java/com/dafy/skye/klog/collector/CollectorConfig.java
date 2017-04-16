package com.dafy.skye.klog.collector;

import java.util.Properties;

/**
 * Created by Caedmon on 2017/4/16.
 */
public class CollectorConfig {
    private String topic;
    private int partition;
    private String groupId;

    public CollectorConfig(String topic,String groupId,int partition) {
        this.topic = topic;
        this.partition = partition;
        this.groupId = groupId;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public int getPartition() {
        return partition;
    }

    public void setPartition(int partition) {
        this.partition = partition;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
    public static class Builder{
        private String topic;
        private int partition;
        private String groupId;
        public static final String PROPERTIES_PREFIX="skye-klog-collector";
        public Builder topic(String topic){
            this.topic=topic;
            return this;
        }
        public Builder partition(int partition){
            this.partition=partition;
            return this;
        }
        public Builder groupId(String groupId){
            this.groupId=groupId;
            return this;
        }
        public static Builder create(){
            return new Builder();
        }
        public CollectorConfig build(){
            return new CollectorConfig(this.topic,this.groupId,this.partition);
        }
        public CollectorConfig build(Properties properties){
            this.topic=properties.getProperty(PROPERTIES_PREFIX+".topic","skye-klog");
            this.groupId=properties.getProperty(PROPERTIES_PREFIX+".groupId","skye-klog");
            this.partition=Integer.parseInt(properties.getProperty(PROPERTIES_PREFIX+".partition","0"));
            return this.build();
        }
    }
}
