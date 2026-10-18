package com.ideafactory.utility.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class ApiVersionInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(ApiVersionInterceptor.class);

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        String apiVersion = request.getHeader("X-API-Version");
        if (apiVersion != null) {
            logger.debug("API Version: {}", apiVersion);
        }
        return true;
    }
}