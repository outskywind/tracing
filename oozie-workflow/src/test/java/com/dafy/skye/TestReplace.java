package com.dafy.skye;

import org.junit.Test;

/**
 * Created by quanchengyun on 2017/11/2.
 */
public class TestReplace {



    @Test
    public void test() {

        System.out.println("today is {date} hhh ddd kkk{date}".replaceAll("\\{date\\}","20171102"));
    }
}
