package com.ericsson.ei.config;

import java.util.concurrent.Executor;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class SpringAsyncConfig implements AsyncConfigurer{

    @Value("${threads.corePoolSize}") private int corePoolSize;
    @Value("${threads.queueCapacity}") private int queueCapacity;
    @Value("${threads.maxPoolSize}") private int maxPoolSize;


    @Override
    public Executor getAsyncExecutor() {
         ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
            executor.setCorePoolSize(corePoolSize);
            executor.setQueueCapacity(queueCapacity);
            executor.setMaxPoolSize(maxPoolSize);
            executor.setThreadNamePrefix("EventHandler-");
            executor.initialize();
            return executor;
    }


    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        // TODO Auto-generated method stub
        return null;
    }
}
