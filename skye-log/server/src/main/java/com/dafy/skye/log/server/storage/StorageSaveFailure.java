package com.dafy.skye.log.server.storage;

import com.dafy.skye.log.core.logback.SkyeLogEvent;

/**
 * Created by Caedmon on 2017/6/8.
 */
public class StorageSaveFailure {
    private String failReason;
    private SkyeLogEvent failEvent;
    private boolean success;

    private StorageSaveFailure(Builder builder) {
        setFailReason(builder.failReason);
        setFailEvent(builder.failEvent);
        setSuccess(builder.success);
    }

    public String getFailReason() {
        return failReason;
    }

    public void setFailReason(String failReason) {
        this.failReason = failReason;
    }

    public SkyeLogEvent getFailEvent() {
        return failEvent;
    }

    public void setFailEvent(SkyeLogEvent failEvent) {
        this.failEvent = failEvent;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
    public static Builder newBuilder(){
        return new Builder();
    }
    public static final class Builder {
        private String failReason;
        private SkyeLogEvent failEvent;
        private boolean success;

        public Builder() {
        }

        public Builder failReason(String val) {
            failReason = val;
            return this;
        }

        public Builder failEvent(SkyeLogEvent val) {
            failEvent = val;
            return this;
        }

        public Builder success(boolean val) {
            success = val;
            return this;
        }

        public StorageSaveFailure build() {
            return new StorageSaveFailure(this);
        }
    }
}
