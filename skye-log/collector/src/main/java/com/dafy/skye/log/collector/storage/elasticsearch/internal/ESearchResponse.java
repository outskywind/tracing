package com.dafy.skye.log.collector.storage.elasticsearch.internal;

import java.util.List;

/**
 * Created by Caedmon on 2017/6/7.
 */
public class ESearchResponse<T> {
    private boolean time_out;
    private Integer took;
    private Shard _shards;
    private Hits<T> hits;
    private Integer status;
    private ErrorCause error;
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

    public Hits<T> getHits() {
        return hits;
    }

    public void setHits(Hits<T> hits) {
        this.hits = hits;
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
    public boolean isSucces(){
        return status==null||status==0||status==200;
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
    public static class Hits<T>{
        private List<Document<T>> hits;
        private Integer total;

        public List<Document<T>> getHits() {
            return hits;
        }

        public void setHits(List<Document<T>> hits) {
            this.hits = hits;
        }

        public Integer getTotal() {
            return total;
        }

        public void setTotal(Integer total) {
            this.total = total;
        }

        @Override
        public String toString() {
            return "Hits{" +
                    "hits=" + hits +
                    ", total=" + total +
                    '}';
        }
    }
    public static class Document<T>{
        private String _index;
        private String _type;
        private T _source;
        private String id;

        public String get_index() {
            return _index;
        }

        public void set_index(String _index) {
            this._index = _index;
        }

        public String get_type() {
            return _type;
        }

        public void set_type(String _type) {
            this._type = _type;
        }

        public T get_source() {
            return _source;
        }

        public void set_source(T _source) {
            this._source = _source;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        @Override
        public String toString() {
            return "Document{" +
                    "_index='" + _index + '\'' +
                    ", _type='" + _type + '\'' +
                    ", _source=" + _source +
                    ", id='" + id + '\'' +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "ESearchResponse{" +
                "time_out=" + time_out +
                ", took=" + took +
                ", _shards=" + _shards +
                ", hits=" + hits +
                ", status=" + status +
                ", error=" + error +
                '}';
    }
}
