package com.interviewcoach.gateway.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class HealthController {

    private final WebClient webClient;

    @Value("${app.services.core-service}")
    private String coreServiceUrl;

    @Value("${app.services.user-service}")
    private String userServiceUrl;

    @Value("${app.services.media-service}")
    private String mediaServiceUrl;

    @Value("${app.services.utility-service}")
    private String utilityServiceUrl;

    @Value("${app.services.notification-service}")
    private String notificationServiceUrl;

    public HealthController(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    @GetMapping("/api/health")
    public Mono<ResponseEntity<Map<String, Object>>> health() {
        Map<String, String> serviceUrls = new LinkedHashMap<>();
        serviceUrls.put("core-service", coreServiceUrl);
        serviceUrls.put("user-service", userServiceUrl);
        serviceUrls.put("media-service", mediaServiceUrl);
        serviceUrls.put("utility-service", utilityServiceUrl);
        serviceUrls.put("notification-service", notificationServiceUrl);

        // Ping all services in parallel (fire-and-forget style with timeout)
        var serviceChecks = serviceUrls.entrySet().stream()
                .map(entry -> checkService(entry.getKey(), entry.getValue()))
                .toList();

        return Mono.zip(serviceChecks, results -> {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("status", "UP");
            Map<String, Object> services = new LinkedHashMap<>();
            boolean allUp = true;

            for (Object result : results) {
                @SuppressWarnings("unchecked")
                Map<String, Object> serviceStatus = (Map<String, Object>) result;
                String name = (String) serviceStatus.get("name");
                String status = (String) serviceStatus.get("status");
                services.put(name, serviceStatus);
                if (!"UP".equals(status)) {
                    allUp = false;
                }
            }

            response.put("status", allUp ? "UP" : "DEGRADED");
            response.put("services", services);
            return ResponseEntity.ok(response);
        });
    }

    private Mono<Map<String, Object>> checkService(String name, String url) {
        return webClient.get()
                .uri(url + "/actuator/health")
                .exchangeToMono(response -> {
                    Map<String, Object> result = new LinkedHashMap<>();
                    result.put("name", name);
                    result.put("url", url);
                    boolean isUp = response.statusCode().is2xxSuccessful();
                    result.put("status", isUp ? "UP" : "DOWN");
                    if (!isUp) {
                        result.put("error", "HTTP " + response.statusCode().value());
                    }
                    return Mono.just(result);
                })
                .timeout(Duration.ofSeconds(10))
                .onErrorResume(ex -> {
                    Map<String, Object> result = new LinkedHashMap<>();
                    result.put("name", name);
                    result.put("status", "DOWN");
                    result.put("url", url);
                    result.put("error", ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName());
                    return Mono.just(result);
                });
    }
}
