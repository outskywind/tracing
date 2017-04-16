package com.dafy.skye.klog.collector.consumer;

import com.dafy.skye.klog.core.logback.KLogEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Caedmon on 2017/4/16.
 */
public class PollResult {
    private List<KLogEvent> events;
    private long endOffset;
    public PollResult(int capacity){
        this.events=new ArrayList<>(capacity);
    }
    public List<KLogEvent> getEvents() {
        return events;
    }

    public void setEvents(List<KLogEvent> events) {
        this.events = events;
    }

    public long getEndOffset() {
        return endOffset;
    }

    public void setEndOffset(long endOffset) {
        this.endOffset = endOffset;
    }

    public void addKLog(KLogEvent event){
        events.add(event);
    }
    public boolean isEmpty(){
        return events.isEmpty();
    }
    public int size(){
        return events.size();
    }
}
