package com.interviewcoach.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.security.Key;
import java.util.List;

@Component
public class JwtValidationFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(JwtValidationFilter.class);

    private static final List<String> PUBLIC_PATHS = List.of(
        "/api/auth/",
        "/oauth2/",
        "/login",
        "/api/interviews/guest",
        "/api/interviews/init",
        "/api/interviews/fetch",
        "/api/interviews/job-page",
        "/api/interviews/job-details",
        "/api/interviews/resume-preview",
        "/api/interviews/resume-upload",
        "/api/interviews/resume-parse",
        "/api/interviews/",
        "/api/faqs",
        "/api/faqs/",
        "/api/room/",
        "/api/tts/",
        "/actuator/",
        "/api/users/actuator/",
        "/api/ai/actuator/",
        "/media/actuator/",
        "/notifications/actuator/"
    );

    private final Key key;
    private final String apiVersion;

    public JwtValidationFilter(
            @Value("${jwt.secret}") String secret,
            @Value("${api.version:v1}") String apiVersion) {
        byte[] keyBytes = secret.getBytes();
        if (keyBytes.length < 32) {
            String paddedSecret = String.format("%-32s", secret).replace(' ', '0');
            keyBytes = paddedSecret.getBytes();
        }
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.apiVersion = apiVersion;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        if (isPublicPath(path)) {
            exchange.getResponse().getHeaders().set("X-API-Version", apiVersion);
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.warn("Missing or invalid Authorization header for path: {}", path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            exchange.getResponse().getHeaders().set("X-API-Version", apiVersion);
            return exchange.getResponse().setComplete();
        }

        String jwt = authHeader.substring(7);

        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(jwt)
                    .getBody();

            ServerWebExchange mutatedExchange = exchange.mutate()
                    .request(r -> r.header("X-User-Email", claims.getSubject()))
                    .build();

            return chain.filter(mutatedExchange);
        } catch (Exception e) {
            logger.warn("Invalid JWT token for path: {}", path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            exchange.getResponse().getHeaders().set("X-API-Version", apiVersion);
            return exchange.getResponse().setComplete();
        }
    }

    @Override
    public int getOrder() {
        return -100;
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }
}
