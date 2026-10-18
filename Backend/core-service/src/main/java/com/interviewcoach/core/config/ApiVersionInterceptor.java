package com.interviewcoach.core.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@SuppressWarnings("null")
public class ApiVersionInterceptor implements HandlerInterceptor {

    private final String apiVersion;

    public ApiVersionInterceptor(@Value("${api.version:v1}") String apiVersion) {
        this.apiVersion = apiVersion;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        response.setHeader("X-API-Version", apiVersion);
        response.setHeader("X-API-Deprecation-Notice", "false");
        return true;
    }
}
