package com.dafy.skye.component;

import java.util.Collection;

/**
 * Created by quanchengyun on 2017/9/19.
 */
public interface Strategy {

    String chooseNext(Collection candidates);

    <T> T chooseNext(T[] candidates);
}
