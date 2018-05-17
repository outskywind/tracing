package com.dafy.skye.elasticsearch.template;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by quanchengyun on 2018/5/9.
 */
public class ResourceIndexTemplate extends IndexTemplate {

    private String resource;


    public void setResource(String resource) {
        this.resource = resource;
    }

    @Override
    public String resolveTemplate() throws Exception{
        InputStream in = null;
        if(resource!=null){
            //果然是在所有的classpath 下面加载资源文件。
            in  = this.getClass().getClassLoader().getResourceAsStream(resource);
            if(in==null){
                throw new RuntimeException("index template " + resource + "can't be load from classpath");
            }
            try{
                //since this is a local file inputStream read all bytes at once
                byte[] b = new byte[in.available()];
                String json;
                if(in.read(b)>0){
                    json = new String(b,"UTF-8");
                    return json;
                }
            }catch(IOException e){
                throw  new IOException("索引配置文件读取失败",e);
            }finally {
                if(in!=null){
                    in.close();
                }
            }
        }
        return null;
    }
}
