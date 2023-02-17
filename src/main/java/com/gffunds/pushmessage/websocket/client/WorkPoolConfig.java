package com.gffunds.pushmessage.websocket.client;
 
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
 
import java.util.concurrent.ThreadPoolExecutor;
 
/**
 * @author Curtain
 * @date 2021/11/1
 */
@Configuration
public class WorkPoolConfig {
 
    @Value("${settings.work-pool.core-pool-size:10}")
    private Integer workPoolCoreSize;
 
    @Value("${settings.work-pool.max-pool-size:10}")
    private Integer workPoolMaxSize;
 
    @Value("${settings.work-pool.queue-capacity:10}")
    private Integer queueCapacity;
 
    @Bean("workPoolScheduler")
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(workPoolCoreSize);
        executor.setMaxPoolSize(workPoolMaxSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("-device-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        return executor;
    }
}
 