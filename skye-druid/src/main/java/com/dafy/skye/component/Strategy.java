package com.dafy.skye.component;

import java.util.Collection;

/**
 * Created by quanchengyun on 2017/9/19.
 */
public interface Strategy {

    public ServerInfo chooseNext(Collection<ServerInfo> candidates);
}
