package com.ideafactory.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

@Component
public class RequestLoggingFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);

    private static final String START_TIME_ATTR = "requestStartTime";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        Instant startTime = Instant.now();
        exchange.getAttributes().put(START_TIME_ATTR, startTime);

        ServerHttpRequest request = exchange.getRequest();
        String method = request.getMethod().name();
        String path = request.getURI().getPath();
        String query = request.getURI().getQuery();
        String fullPath = query != null ? path + "?" + query : path;

        return chain.filter(exchange)
                .doOnSuccess(aVoid -> logResponse(exchange, method, fullPath, startTime))
                .doOnError(error -> logError(exchange, method, fullPath, startTime, error));
    }

    private void logResponse(ServerWebExchange exchange, String method, String path, Instant startTime) {
        ServerHttpResponse response = exchange.getResponse();
        var httpStatusCode = response.getStatusCode();
        int statusCode = httpStatusCode != null ? httpStatusCode.value() : 0;
        Duration duration = Duration.between(startTime, Instant.now());
        double seconds = duration.toNanos() / 1_000_000_000.0;

        logger.info("{} {} -> {} in {}s",
                method,
                path,
                statusCode,
                String.format("%.3f", seconds));
    }

    private void logError(ServerWebExchange exchange, String method, String path, Instant startTime, Throwable error) {
        Duration duration = Duration.between(startTime, Instant.now());
        double seconds = duration.toNanos() / 1_000_000_000.0;

        logger.error("{} {} -> ERROR in {}s: {}",
                method,
                path,
                String.format("%.3f", seconds),
                error.getMessage());
    }

    @Override
    public int getOrder() {
        return -200;
    }
}