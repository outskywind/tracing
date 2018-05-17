package com.dafy.skye.zipkin.extend;

import com.dafy.skye.zipkin.extend.service.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Created by quanchengyun on 2018/4/11.
 */
@RunWith(SpringJUnit4ClassRunner.class)
//这里看源代码是使用的 SpringBootTestContextBootstrapper 启动的，而main类是用来解析Bean依赖用，context是不一样的
//因此那里使用的 ${env} 必须在 SpringBootTestContextBootstrapper 的启动中添加进去
@SpringBootTest(classes = ZipkinExtendApplication.class,webEnvironment = SpringBootTest.WebEnvironment.NONE,
                properties={"env=dev"})
public class UserServiceTest {

    @Autowired
    UserService userService;

    @Test
    public void   testAddUser(){
    }

}
