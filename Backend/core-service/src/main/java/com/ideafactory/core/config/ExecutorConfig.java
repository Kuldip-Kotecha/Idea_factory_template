package com.ideafactory.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import java.util.concurrent.Executor;

@Configuration
public class ExecutorConfig {

    @Bean(name = "interviewTaskExecutor")
    public Executor interviewTaskExecutor() {
        SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor();
        executor.setVirtualThreads(true);
        executor.setThreadNamePrefix("interview-");
        return executor;
    }
}
