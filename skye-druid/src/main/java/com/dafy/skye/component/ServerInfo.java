package com.dafy.skye.component;

/**
 * Created by quanchengyun on 2017/9/19.
 */
public class ServerInfo {

    private String host;

    private int connections;

    private int avgRespTime;

    private int most99RespTime;
    /**
     * server列表排序用,hash使用
     * 使用key与 serverId 分别hash排序后选择最近的(const-hash)
     */
    private String serverId;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getConnections() {
        return connections;
    }

    public void setConnections(int connections) {
        this.connections = connections;
    }

    public int getAvgRespTime() {
        return avgRespTime;
    }

    public void setAvgRespTime(int avgRespTime) {
        this.avgRespTime = avgRespTime;
    }

    public int getMost99RespTime() {
        return most99RespTime;
    }

    public void setMost99RespTime(int most99RespTime) {
        this.most99RespTime = most99RespTime;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }
}
