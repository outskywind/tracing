package com.dafy.skye.server.dto;

import java.util.Date;

/**
 * Created by Caedmon on 2017/6/5.
 */
public class SkyeLogDTO {
    private Date timestamp;
    private String traceId;
    private String serviceName;
    private String level;
    private String formattedMessage;

    private SkyeLogDTO(Builder builder) {
        setTimestamp(builder.timestamp);
        setTraceId(builder.traceId);
        setServiceName(builder.serviceName);
        setLevel(builder.level);
        setFormattedMessage(builder.formattedMessage);
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getFormattedMessage() {
        return formattedMessage;
    }

    public void setFormattedMessage(String formattedMessage) {
        this.formattedMessage = formattedMessage;
    }

    public static final class Builder {
        private Date timestamp;
        private String traceId;
        private String serviceName;
        private String level;
        private String formattedMessage;

        public Builder() {
        }

        public Builder timestamp(Date val) {
            timestamp = val;
            return this;
        }

        public Builder traceId(String val) {
            traceId = val;
            return this;
        }

        public Builder serviceName(String val) {
            serviceName = val;
            return this;
        }

        public Builder level(String val) {
            level = val;
            return this;
        }

        public Builder formattedMessage(String val) {
            formattedMessage = val;
            return this;
        }

        public SkyeLogDTO build() {
            return new SkyeLogDTO(this);
        }
    }
}
