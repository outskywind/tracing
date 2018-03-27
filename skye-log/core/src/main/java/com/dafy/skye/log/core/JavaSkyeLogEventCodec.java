package com.dafy.skye.log.core;

import com.dafy.skye.log.core.logback.SkyeLogEvent;

import java.io.*;

/**
 * Created by Caedmon on 2017/5/8.
 */
public class JavaSkyeLogEventCodec implements SkyeLogEventCodec {

    @Override
    public SkyeLogEvent decode(byte[] value) throws Exception {

        ByteArrayInputStream bis=new ByteArrayInputStream(value);
        ObjectInputStream ois=new ObjectInputStream(bis);
        return (SkyeLogEvent) ois.readObject();
    }

    @Override
    public byte[] encode(SkyeLogEvent event) {
        ByteArrayOutputStream bos=new ByteArrayOutputStream();
        try {
            ObjectOutputStream ex = new ObjectOutputStream(bos);
            ex.writeObject(event);
            return bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
