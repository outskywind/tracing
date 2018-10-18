/*
package com.dafy.skye.brave.dubbo;

import com.alibaba.dubbo.rpc.Result;
import com.github.kristofa.brave.ClientResponseAdapter;
import com.github.kristofa.brave.KeyValueAnnotation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

*/
/**
 * Created by Caedmon on 2017/4/11.
 *//*

public class DubboClientResponseAdapter implements ClientResponseAdapter {
    private Result result;
    private Throwable exception;
    public DubboClientResponseAdapter(Result result){
        this.result=result;
    }
    public DubboClientResponseAdapter(Throwable exception){
        this.exception=exception;
    }
    public Collection<KeyValueAnnotation> responseAnnotations() {
        List<KeyValueAnnotation> annotations = new ArrayList<KeyValueAnnotation>();
        if(exception != null){
            KeyValueAnnotation keyValueAnnotation=  KeyValueAnnotation.create("exception",exception.getMessage());
            annotations.add(keyValueAnnotation);
        }else{
            if(result.hasException()){
                Throwable throwable=result.getException();
                KeyValueAnnotation keyValueAnnotation=  KeyValueAnnotation.create("exception",throwable.getMessage());
                annotations.add(keyValueAnnotation);
            }else{
                KeyValueAnnotation keyValueAnnotation=  KeyValueAnnotation.create("status","success");
                annotations.add(keyValueAnnotation);
            }
        }
        return annotations;
    }
}
*/
