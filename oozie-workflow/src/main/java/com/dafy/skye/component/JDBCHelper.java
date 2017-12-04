package com.dafy.skye.component;

import java.sql.ResultSet;
import java.util.concurrent.*;

/**
 * Created by quanchengyun on 2017/11/5.
 */
public class JDBCHelper {

    private static ExecutorService executor = Executors.newCachedThreadPool();

    private JDBCHelper(){

    }

    public static  JDBCHelper build(){
        return new JDBCHelper();
    }

    private QueryProcessor query;

    private ResultSetProcessor processor;

    public JDBCHelper withQuery(QueryProcessor query){
        this.query = query;
        return this;
    }

    public JDBCHelper withResultSetProcessor(ResultSetProcessor processor){
        this.processor = processor;
        return this;
    }

    private static class Caller<V> implements Callable<V>{

        public Caller(JDBCHelper helper){
            this.helper = helper;
        }
        JDBCHelper helper ;

        @Override
        public V call() throws Exception {
            ResultSet rs = null;
            try{
                rs = helper.query.query();
                return helper.processor.process(rs);
            }finally {
                if(rs!=null){
                    rs.close();
                }
            }
        }
    }

    public <T> Future<T> run(){
        return executor.submit(new Caller<T>(this));
    }

}
