/*
   Copyright 2017 Ericsson AB.
   For a full list of individual contributors, please see the commit history.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package com.ericsson.ei.config;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class SpringAsyncConfig implements AsyncConfigurer{


    @Value("${threads.corePoolSize}")
    private int eventHandlerCorePoolSize;

    @Value("${threads.queueCapacity}")
    private int eventHandlerQueueCapacity;

    @Value("${threads.maxPoolSize}")
    private int eventHandlerMaxPoolSize;

    @Value("${subscription-handler.threads.corePoolSize:150}")
    private int subscriptionHandlerCorePoolSize;

    @Value("${subscription-handler.threads.queueCapacity:5000}")
    private int subscriptionHandlerQueueCapacity;

    @Value("${subscription-handler.threads.maxPoolSize:150}")
    private int subscriptionHandlerMaxPoolSize;


    @Bean("subscriptionHandlerExecutor")
    public Executor subscriptionHandlerExecutor() {
         final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
            executor.setCorePoolSize(subscriptionHandlerCorePoolSize);
            executor.setQueueCapacity(subscriptionHandlerQueueCapacity);
            executor.setMaxPoolSize(subscriptionHandlerMaxPoolSize);
            executor.setThreadNamePrefix("SubscriptionHandler-");
            executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
            executor.initialize();
            return executor;
    }


    @Bean("eventHandlerExecutor")
    public Executor eventHandlerExecutor() {
         final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
            executor.setCorePoolSize(eventHandlerCorePoolSize);
            executor.setQueueCapacity(eventHandlerQueueCapacity);
            executor.setMaxPoolSize(eventHandlerMaxPoolSize);
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
