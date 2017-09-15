package com.dafy.skye.brave.dubbo;

import com.alibaba.dubbo.rpc.Result;
import com.github.kristofa.brave.KeyValueAnnotation;
import com.github.kristofa.brave.ServerResponseAdapter;
import org.slf4j.MDC;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Caedmon on 2017/4/11.
 */
public class DubboServerResponseAdapter implements ServerResponseAdapter {
    private Result result;
    public DubboServerResponseAdapter(Result result){
        this.result=result;
    }
    public Collection<KeyValueAnnotation> responseAnnotations() {
        List<KeyValueAnnotation> annotations = new ArrayList<KeyValueAnnotation>();
        if(!result.hasException()){
            Object value=result.getValue();
            String valueStr = value==null?"null":String.valueOf(value);
            KeyValueAnnotation keyValueAnnotation=KeyValueAnnotation.create("result",valueStr);
            annotations.add(keyValueAnnotation);
            //加上status状态，统计成功数使用
            KeyValueAnnotation statusAnnotation=  KeyValueAnnotation.create("status","success");
            annotations.add(keyValueAnnotation);
        }else {
            KeyValueAnnotation keyValueAnnotation=  KeyValueAnnotation.create("exception",
                    result.getException().getMessage());
            annotations.add(keyValueAnnotation);
        }
        return annotations;
    }
}
