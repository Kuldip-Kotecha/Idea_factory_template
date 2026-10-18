package com.interviewcoach.notification.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/notifications")
@SuppressWarnings("null")
public class NotificationController {

    private static final String EMAIL_NOTIFICATIONS_QUEUE = "email-notifications";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public NotificationController(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/send-email")
    public String triggerEmail(@RequestBody Map<String, String> payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            redisTemplate.opsForList().leftPush(EMAIL_NOTIFICATIONS_QUEUE, json);
            return "Email queued for delivery";
        } catch (JsonProcessingException e) {
            return "Failed to queue email: " + e.getMessage();
        }
    }
}
