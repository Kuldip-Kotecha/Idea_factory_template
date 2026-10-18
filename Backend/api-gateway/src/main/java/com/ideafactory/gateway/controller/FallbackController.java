package com.ideafactory.gateway.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @Value("${app.fallback.core-service.error}")
    private String coreServiceError;

    @Value("${app.fallback.core-service.message}")
    private String coreServiceMessage;

    @Value("${app.fallback.utility-service.error}")
    private String utilityServiceError;

    @Value("${app.fallback.utility-service.message}")
    private String utilityServiceMessage;

    @Value("${app.fallback.media-service.error}")
    private String mediaServiceError;

    @Value("${app.fallback.media-service.message}")
    private String mediaServiceMessage;

    @Value("${app.fallback.user-service.error}")
    private String userServiceError;

    @Value("${app.fallback.user-service.message}")
    private String userServiceMessage;

    @Value("${app.fallback.notification-service.error}")
    private String notificationServiceError;

    @Value("${app.fallback.notification-service.message}")
    private String notificationServiceMessage;

    @GetMapping("/core-service")
    public ResponseEntity<Map<String, Object>> coreServiceFallbackGet() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "error", coreServiceError,
                        "message", coreServiceMessage
                ));
    }

    @PostMapping("/core-service")
    public ResponseEntity<Map<String, Object>> coreServiceFallbackPost() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "error", coreServiceError,
                        "message", coreServiceMessage
                ));
    }

    @GetMapping("/utility-service")
    public ResponseEntity<Map<String, Object>> aiServiceFallbackGet() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "error", utilityServiceError,
                        "message", utilityServiceMessage
                ));
    }

    @PostMapping("/utility-service")
    public ResponseEntity<Map<String, Object>> aiServiceFallbackPost() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "error", utilityServiceError,
                        "message", utilityServiceMessage
                ));
    }

    @GetMapping("/media-service")
    public ResponseEntity<Map<String, Object>> mediaServiceFallbackGet() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "error", mediaServiceError,
                        "message", mediaServiceMessage
                ));
    }

    @PostMapping("/media-service")
    public ResponseEntity<Map<String, Object>> mediaServiceFallbackPost() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "error", mediaServiceError,
                        "message", mediaServiceMessage
                ));
    }

    @GetMapping("/user-service")
    public ResponseEntity<Map<String, Object>> userServiceFallbackGet() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "error", userServiceError,
                        "message", userServiceMessage
                ));
    }

    @PostMapping("/user-service")
    public ResponseEntity<Map<String, Object>> userServiceFallbackPost() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "error", userServiceError,
                        "message", userServiceMessage
                ));
    }

    @GetMapping("/notification-service")
    public ResponseEntity<Map<String, Object>> notificationServiceFallbackGet() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "error", notificationServiceError,
                        "message", notificationServiceMessage
                ));
    }

    @PostMapping("/notification-service")
    public ResponseEntity<Map<String, Object>> notificationServiceFallbackPost() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "error", notificationServiceError,
                        "message", notificationServiceMessage
                ));
    }
}