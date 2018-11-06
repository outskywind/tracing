package com.dafy.skye.zipkin.extend.config;

/*import com.dafy.elasticjob.spring.boot.autoconfigure.ElasticConfigHelper;
import com.dafy.skye.zipkin.extend.job.ESIndexCloseJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;*/

/**
 * Created by quanchengyun on 2017/7/10.
 */
//@Configuration
@Deprecated
public class JobConfig {
    /*@Autowired
    private ElasticConfigHelper elasticConfigHelper;

    @Bean
    public ESIndexCloseJob mySimpleJob(@Value("${elasticjob.simpleJob.cron}") final String cron,
                                   @Value("${elasticjob.simpleJob.shardingTotalCount}") final int shardingTotalCount,
                                   @Value("${elasticjob.simpleJob.shardingItemParameters}") final String shardingItemParameters) {
        ESIndexCloseJob mySimpleJob=new ESIndexCloseJob();
        elasticConfigHelper.initSimpleJobScheduler(mySimpleJob,cron,shardingTotalCount,shardingItemParameters);
        return mySimpleJob;
    }*/
}
