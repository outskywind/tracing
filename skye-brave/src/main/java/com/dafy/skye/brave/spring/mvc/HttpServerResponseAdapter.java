package com.dafy.skye.brave.spring.mvc;

import com.github.kristofa.brave.KeyValueAnnotation;
import com.github.kristofa.brave.ServerResponseAdapter;
import com.github.kristofa.brave.http.HttpResponse;
import zipkin.TraceKeys;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by quanchengyun on 2018/6/11.
 */
public class HttpServerResponseAdapter implements ServerResponseAdapter {

    private final HttpResponse response;

    public HttpServerResponseAdapter(HttpResponse response)
    {
        this.response = response;
    }

    @Override
    public Collection<KeyValueAnnotation> responseAnnotations() {
        List<KeyValueAnnotation> annotationList = new ArrayList<>();
        annotationList.add(KeyValueAnnotation.create(
                TraceKeys.HTTP_STATUS_CODE, String.valueOf(response.getHttpStatusCode())));
        if(200<=response.getHttpStatusCode() && response.getHttpStatusCode()<300){
            annotationList.add(KeyValueAnnotation.create(
                    "status", "success"));
        }
        return annotationList;
    }
}
