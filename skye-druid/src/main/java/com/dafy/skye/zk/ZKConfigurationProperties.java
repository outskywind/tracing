package com.dafy.skye.zk;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Created by quanchengyun on 2017/9/18.
 */
@ConfigurationProperties(prefix="druid.zk",ignoreInvalidFields=true,exceptionIfInvalid=false)
public class ZKConfigurationProperties {

    private String namespace;

    private String host;

    private String path;

    private int connectTimeouts=30000;

    private int sessionTimeouts=30000;

    private boolean readOnly=false;

    private int retryConnectIntervalMs=10000;


    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getConnectTimeouts() {
        return connectTimeouts;
    }

    public void setConnectTimeouts(int connectTimeouts) {
        this.connectTimeouts = connectTimeouts;
    }

    public int getSessionTimeouts() {
        return sessionTimeouts;
    }

    public void setSessionTimeouts(int sessionTimeouts) {
        this.sessionTimeouts = sessionTimeouts;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public int getRetryConnectIntervalMs() {
        return retryConnectIntervalMs;
    }

    public void setRetryConnectIntervalMs(int retryConnectIntervalMs) {
        this.retryConnectIntervalMs = retryConnectIntervalMs;
    }
}
