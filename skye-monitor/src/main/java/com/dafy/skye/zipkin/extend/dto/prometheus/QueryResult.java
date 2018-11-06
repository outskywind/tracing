package com.dafy.skye.zipkin.extend.dto.prometheus;

/**
 * Created by quanchengyun on 2018/10/21.
 */
public class QueryResult {

    private  String status;

    private  Data data;


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }
}
