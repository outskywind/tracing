package com.dafy.skye.util;

import java.io.InputStream;
import java.util.Properties;

/**
 * Created by quanchengyun on 2018/10/12.
 */
public class ClientVersionUtil {

    public static final String versionFile="/META-INF/maven/com.dafy.skye/skye-autoconfigure/pom.properties";
    public static final String versionKey="version";

    private static final String version=getVersion();

    public static String version(){
        return version;
    }

    private static String getVersion(){
        InputStream ins = null;
        try{
            ins = ClientVersionUtil.class.getResourceAsStream(versionFile);
            if(ins!=null){
                Properties  p = new Properties();
                p.load(ins);
                return p.getProperty(versionKey);
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try{
                ins.close();
            }catch (Throwable throwable){
                //
            }
        }
        return "unknown";
    }
}
