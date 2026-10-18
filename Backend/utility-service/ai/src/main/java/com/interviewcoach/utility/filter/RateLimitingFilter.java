package com.interviewcoach.utility.filter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Component
@SuppressWarnings({"null", "deprecation"})
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitingFilter.class);

    private final int capacity;
    private final int refillTokens;
    private final Duration refillDuration;
    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    public RateLimitingFilter(
            @Value("${rate.limit.capacity:30}") int capacity,
            @Value("${rate.limit.refill-tokens:10}") int refillTokens,
            @Value("${rate.limit.refill-minutes:1}") int refillMinutes) {
        this.capacity = capacity;
        this.refillTokens = refillTokens;
        this.refillDuration = Duration.ofMinutes(refillMinutes);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String clientKey = getClientKey(request);
        Bucket bucket = buckets.computeIfAbsent(clientKey, k -> createNewBucket());

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            logger.warn("Rate limit exceeded for client: {}", clientKey);
            response.setStatus(429);
            response.setHeader("Retry-After", String.valueOf(refillDuration.getSeconds()));
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Rate limit exceeded. Try again later.\"}");
        }
    }

    private String getClientKey(HttpServletRequest request) {
        String userEmail = request.getHeader("X-User-Email");
        if (userEmail != null && !userEmail.isBlank()) {
            return userEmail;
        }
        return request.getRemoteAddr();
    }

    private Bucket createNewBucket() {
        Refill refill = Refill.intervally(refillTokens, refillDuration);
        Bandwidth limit = Bandwidth.classic(capacity, refill);
        return Bucket.builder().addLimit(limit).build();
    }
}