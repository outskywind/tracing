package com.dafy.skye;

import com.dafy.skye.entity.SkyeMetric;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.druid.jackson.DefaultObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Created by quanchengyun on 2017/9/5.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class JacksonTest {
@Autowired
    ObjectMapper objectMapper;

@Test
    public void test(){

    ObjectMapper mapper = new ObjectMapper();

    TestClass cls = new TestClass();
    cls.setName("lkkk");

    SkyeMetric metric = new SkyeMetric();
    metric.setException(123);

    try {
        String value =  mapper.writeValueAsString(cls);
        String value2 =  mapper.writeValueAsString(metric);
        System.out.println(value);
        System.out.println(value2);
    } catch (JsonProcessingException e) {
        e.printStackTrace();
    }

}

    @Test
    public void test2(){


        TestClass cls = new TestClass();
        cls.setName("lkkk");

        SkyeMetric metric = new SkyeMetric();
        metric.setException(123);

        try {
            String value =  objectMapper.writeValueAsString(cls);
            String value2 =  objectMapper.writeValueAsString(metric);
            System.out.println(value);
            System.out.println(value2);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

    }


}



