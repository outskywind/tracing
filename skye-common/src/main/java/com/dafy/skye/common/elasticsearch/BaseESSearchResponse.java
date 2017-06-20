package com.dafy.skye.common.elasticsearch;

import java.util.List;

/**
 * Created by Caedmon on 2017/6/20.
 */
public class BaseESSearchResponse {
    protected boolean time_out;
    protected Integer took;
    protected Shard _shards;
    protected Integer status;
    protected ErrorCause error;

    public boolean isTime_out() {
        return time_out;
    }

    public void setTime_out(boolean time_out) {
        this.time_out = time_out;
    }

    public Integer getTook() {
        return took;
    }

    public void setTook(Integer took) {
        this.took = took;
    }

    public Shard get_shards() {
        return _shards;
    }

    public void set_shards(Shard _shards) {
        this._shards = _shards;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public ErrorCause getError() {
        return error;
    }

    public void setError(ErrorCause error) {
        this.error = error;
    }

    public static class Shard{
        private Integer total;
        private Integer failed;
        private Integer successful;

        public Integer getTotal() {
            return total;
        }

        public void setTotal(Integer total) {
            this.total = total;
        }

        public Integer getFailed() {
            return failed;
        }

        public void setFailed(Integer failed) {
            this.failed = failed;
        }

        public Integer getSuccessful() {
            return successful;
        }

        public void setSuccessful(Integer successful) {
            this.successful = successful;
        }
    }
    public static class ErrorCause {
        private String type;
        private String reason;
        private List<ErrorCause> root_cause;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }

        public List<ErrorCause> getRoot_cause() {
            return root_cause;
        }

        public void setRoot_cause(List<ErrorCause> root_cause) {
            this.root_cause = root_cause;
        }

        @Override
        public String toString() {
            return "ErrorCause{" +
                    "type='" + type + '\'' +
                    ", reason='" + reason + '\'' +
                    ", root_cause=" + root_cause +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "BaseESSearchResponse{" +
                "time_out=" + time_out +
                ", took=" + took +
                ", _shards=" + _shards +
                ", status=" + status +
                ", error=" + error +
                '}';
    }
}
