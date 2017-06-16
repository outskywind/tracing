package com.dafy.skye.log.server;

import com.dafy.skye.common.util.IndexNameFormatter;
import org.junit.Test;

/**
 * Created by Caedmon on 2017/5/2.
 */
public class IndexNameFormatterTest {
    @Test
    public void testIndexName(){
        IndexNameFormatter.Builder builder=IndexNameFormatter.builder();
        builder.index("test");
        System.out.println(builder.build().indexNameForTimestamp(System.currentTimeMillis()));
    }
}
