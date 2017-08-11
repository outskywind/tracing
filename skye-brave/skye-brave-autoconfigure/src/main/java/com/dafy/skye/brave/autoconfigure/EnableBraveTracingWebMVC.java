package com.dafy.skye.brave.autoconfigure;

import com.dafy.skye.brave.spring.mvc.SpringMVCAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Created by quanchengyun on 2017/8/11.
 */
@Target(ElementType.TYPE)
//重要不然运行解析后不会再metadata 信息里
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import({SpringMVCAutoConfiguration.class})
public @interface EnableBraveTracingWebMVC {
}
