package com.interviewcoach.media.config;

import feign.Feign;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignTracingConfig {

    @Bean
    public Feign.Builder feignBuilder() {
        return Feign.builder();
    }
}
