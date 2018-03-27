package com.dafy.skye.conf;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.UrlResource;

/**
 * Created by quanchengyun on 2018/3/16.
 */
public class ConfigCenterResourceLoader implements ResourceLoader {

    @Override
    public Resource getResource(String location) {
        try{
            if(location.startsWith("http://")){
                return  new UrlResource(location);
            }
            return null;
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("urlResource of "+location+" init failed.");
        }
    }

    @Override
    public ClassLoader getClassLoader() {
        return getClass().getClassLoader();
    }
}
