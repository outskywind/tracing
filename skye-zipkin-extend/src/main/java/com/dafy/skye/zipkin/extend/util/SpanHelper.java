package com.dafy.skye.zipkin.extend.util;

import zipkin.BinaryAnnotation;
import zipkin.Span;

import java.util.Base64;

/**
 * Created by Caedmon on 2017/6/20.
 */
public class SpanHelper {

    public static void baValueBase64Decode(Span span){
        if(span.binaryAnnotations!=null){
            for (BinaryAnnotation annotation:span.binaryAnnotations){
                if(annotation.type==BinaryAnnotation.Type.STRING){
                    annotation.toBuilder().value(new String(Base64.getDecoder().decode(annotation.value)));
                }
            }
        }

    }
}
