package com.dafy.skye.common.elasticsearch;

import org.elasticsearch.action.support.IndicesOptions;

/**
 * Created by Caedmon on 2017/6/26.
 */
public class DefaultOptions {
    public static IndicesOptions defaultIndicesOptions(){
        return IndicesOptions.fromOptions(true,true,true,true);
    }
}
