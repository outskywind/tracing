package com.dafy.skye.log.server.autoconfig;

import com.dafy.skye.log.server.controller.LogQueryController;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Created by Caedmon on 2017/4/24.
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({
        LogServerAutoConfiguration.class,LogQueryController.class
}
        )
public @interface EnableSkyeLogServer {
}
