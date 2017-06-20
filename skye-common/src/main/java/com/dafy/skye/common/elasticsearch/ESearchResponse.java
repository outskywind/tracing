package com.dafy.skye.common.elasticsearch;

import java.util.List;

/**
 * Created by Caedmon on 2017/6/7.
 */
public class ESearchResponse<T> extends BaseESSearchResponse{
    private Hits<T> hits;

    public Hits<T> getHits() {
        return hits;
    }

    public void setHits(Hits<T> hits) {
        this.hits = hits;
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
                ", hits=" + hits +
                ", took=" + took +
                ", _shards=" + _shards +
                ", status=" + status +
                ", error=" + error +
                '}';
    }
}
