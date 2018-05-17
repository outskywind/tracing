package com.dafy.skye.elasticsearch.template;

import org.apache.http.entity.StringEntity;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by quanchengyun on 2018/5/9.
 */
public class IndexTemplate {

    private String  template;

    private RestClient client;

    private volatile AtomicBoolean ensured = new AtomicBoolean(false);

    public void setTemplate(String template) {
        this.template = template;
    }

    public void setClient(RestClient client) {
        this.client = client;
    }


    /**
     * 无则设置模板。
     * @param name
     * @throws Exception
     */
    public boolean ensureTemplate(String name) throws Exception{
        if(!ensured.get()){
            synchronized (ensured){
                if(createTemplate(name))ensured.set(true);
            }
        }
        return ensured.get();
    }


    public boolean createTemplate(String name) throws Exception{
        
        if(this.template==null){
            this.template=resolveTemplate();
        }
        if(this.template==null){
            throw new RuntimeException("索引模板不存在");
        }
        StringEntity template = new StringEntity(this.template,"UTF-8");
        Response response = client.performRequest("PUT","_template/"+name,null,template);
        if(response!=null){
            return true;
        }
        return false;
    }

    protected String resolveTemplate() throws Exception{
        return null;
    }

}
