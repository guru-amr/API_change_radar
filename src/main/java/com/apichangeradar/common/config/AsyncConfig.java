package com.apichangeradar.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class AsyncConfig implements AsyncConfigurer {
    @Value("${app.async.core-pool-size:5}") int core;
    @Value("${app.async.max-pool-size:10}") int max;
    @Value("${app.async.queue-capacity:100}") int queue;
    @Value("${app.async.thread-name-prefix:alert-async-}") String prefix;

    @Bean(name="alertTaskExecutor")
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();
        ex.setCorePoolSize(core);
        ex.setMaxPoolSize(max);
        ex.setQueueCapacity(queue);
        ex.setThreadNamePrefix(prefix);
        ex.initialize();
        return ex;
    }
}

