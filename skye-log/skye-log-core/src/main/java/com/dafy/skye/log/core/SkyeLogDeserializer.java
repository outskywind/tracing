package com.dafy.skye.log.core;

import com.dafy.skye.log.core.logback.SkyeLogEvent;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Map;

/**
 * Created by Administrator on 2016/4/1.
 */
public class SkyeLogDeserializer implements Deserializer<SkyeLogEvent>{
    public SkyeLogDeserializer() {
        super();
    }

    @Override
    public void configure(Map<String, ?> map, boolean b) {

    }

    @Override
    public SkyeLogEvent deserialize(String s, byte[] bytes) {

        try {
            ByteArrayInputStream bis=new ByteArrayInputStream(bytes);
            ObjectInputStream ois=new ObjectInputStream(bis);
            return (SkyeLogEvent) ois.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void close() {

    }
}
