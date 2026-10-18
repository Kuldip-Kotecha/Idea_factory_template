package com.ideafactory.utility;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.ideafactory.utility")
public class UtilityServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UtilityServiceApplication.class, args);
    }
}