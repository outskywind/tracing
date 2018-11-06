/*
package com.dafy.skye.brave.dubbo;

import com.alibaba.dubbo.rpc.Result;
import com.github.kristofa.brave.KeyValueAnnotation;
import com.github.kristofa.brave.ServerResponseAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

*/
/**
 * Created by Caedmon on 2017/4/11.
 *//*

public class DubboServerResponseAdapter implements ServerResponseAdapter {
    private Result result;
    private Throwable exception;
    public DubboServerResponseAdapter(Result result){
        this.result=result;
    }
    public DubboServerResponseAdapter(Throwable exception){
        this.exception=exception;
    }


    public Collection<KeyValueAnnotation> responseAnnotations() {
        List<KeyValueAnnotation> annotations = new ArrayList<KeyValueAnnotation>();
        if(!result.hasException() && this.exception==null){
            //加上status状态，统计成功数使用
            KeyValueAnnotation statusAnnotation=  KeyValueAnnotation.create("status","success");
            annotations.add(statusAnnotation);
        }else {
            //null message exceptions
            KeyValueAnnotation keyValueAnnotation=  KeyValueAnnotation.create("exception",
                    result.getException().getMessage()==null?this.exception==null?"":this.exception.getMessage():result.getException().getMessage());
            annotations.add(keyValueAnnotation);
        }
        return annotations;
    }
}
*/
