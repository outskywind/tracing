package com.dafy.skye.component;

/**
 * Created by quanchengyun on 2017/9/21.
 */
public enum GranularitySimple {

    $1MIN("PT1M"),$5MIN("PT5M"),$10MIN("PT10M"),$15MIN("PT15M"),$30MIN("PT30M"),
    $1HOUR("PT1H"),$1DAY("P1D");
    public final String value;

    GranularitySimple(String value){
        this.value = value;
    }

}
