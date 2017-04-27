package com.dafy.skye.log.collector.util;

import com.google.common.io.CharStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * Created by Caedmon on 2017/4/26.
 */
public class ResourceUtil {
    private static final Logger log= LoggerFactory.getLogger(ResourceUtil.class);
    public static InputStream readInputStream(String path){
        InputStream inputStream=ResourceUtil.class.getClassLoader().getResourceAsStream(path);
        return inputStream;
    }
    public static String readString(String path){
        InputStream inputStream=readInputStream(path);
        try (Reader reader = new InputStreamReader(inputStream, "UTF-8")) {
            return CharStreams.toString(reader);
        } catch (IOException ex) {
            log.error(ex.getMessage(), ex);
            return null;
        }
    }
}
