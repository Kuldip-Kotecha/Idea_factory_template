package com.interviewcoach.utility;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.interviewcoach.utility")
public class UtilityServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UtilityServiceApplication.class, args);
    }
}