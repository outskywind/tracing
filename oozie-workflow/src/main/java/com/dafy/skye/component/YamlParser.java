package com.dafy.skye.component;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;

/**
 * Created by quanchengyun on 2017/10/24.
 */
public class YamlParser {


    public static <T> T parse(String pathtoyml , Class<T> configClazz){
        Yaml yaml = new Yaml();
        T config = null;
        try {
            config = yaml.loadAs(Thread.currentThread().getContextClassLoader().getResourceAsStream(pathtoyml), configClazz);
            System.out.println(config);
        } catch (Exception e){
            e.printStackTrace();
        }
        return config;
    }


    /**
     *
     * @param ins
     * @param configClazz
     * @param <T>
     * @return
     */
    public static <T> T parse(InputStream ins , Class<T> configClazz){
        Yaml yaml = new Yaml();
        T config = null;
        try {
            config = yaml.loadAs(ins, configClazz);
            System.out.println(config);
        } catch (Exception e){
            e.printStackTrace();
        }
        return config;
    }

}
