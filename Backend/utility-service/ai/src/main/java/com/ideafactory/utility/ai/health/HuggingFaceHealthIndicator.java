package com.ideafactory.utility.ai.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
public class HuggingFaceHealthIndicator implements HealthIndicator {

    private static final Logger logger = LoggerFactory.getLogger(HuggingFaceHealthIndicator.class);
    private static final String HEALTH_CHECK_PATH = "/v1/models";

    private final WebClient webClient;
    private final String apiKey;
    private final String apiBaseUrl;

    public HuggingFaceHealthIndicator(WebClient webClient,
            @Value("${ai.api-key}") String apiKey,
            @Value("${ai.base-url}") String apiBaseUrl) {
        this.webClient = webClient;
        this.apiKey = apiKey;
        this.apiBaseUrl = apiBaseUrl;
    }

    @Override
    public Health health() {
        try {
            String response = webClient.get()
                .uri(apiBaseUrl + HEALTH_CHECK_PATH)
                .header("Authorization", "Bearer " + apiKey)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(10))
                .onErrorResume(e -> Mono.just("{\"error\":\"" + e.getMessage() + "\"}"))
                .block();

            if (response != null && response.contains("error")) {
                logger.warn("HuggingFace health check returned error: {}", response);
                return Health.down().withDetail("huggingface", "API error").build();
            }
            return Health.up().withDetail("huggingface", "Connected").build();
        } catch (Exception e) {
            logger.error("HuggingFace health check failed", e);
            return Health.down().withDetail("huggingface", "Connection failed: " + e.getMessage()).build();
        }
    }
}