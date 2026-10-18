package com.interviewcoach.user.queue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@SuppressWarnings("null")
public class RedisQueueServiceImpl implements QueueService {

    private static final Logger logger = LoggerFactory.getLogger(RedisQueueServiceImpl.class);

    private static final String EMAIL_NOTIFICATIONS_QUEUE = "email-notifications";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public RedisQueueServiceImpl(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publishUserDeleted(Long userId, String email) {
        try {
            Map<String, Object> event = Map.of(
                    "userId", userId,
                    "email", email
            );
            String json = objectMapper.writeValueAsString(event);
            redisTemplate.opsForList().leftPush("user-deleted", json);
            redisTemplate.opsForList().leftPush("user-deleted-media", json);
            logger.info("Published user-deleted event to both queues: userId={}", userId);
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize user-deleted event for user {}", userId, e);
        }
    }

    @Override
    public void publishEmailNotification(String to, String subject, String text, String ctaUrl) {
        try {
            Map<String, String> payload = Map.of(
                    "to", to,
                    "subject", subject,
                    "text", text,
                    "ctaUrl", ctaUrl != null ? ctaUrl : ""
            );
            String json = objectMapper.writeValueAsString(payload);
            redisTemplate.opsForList().leftPush(EMAIL_NOTIFICATIONS_QUEUE, json);
            logger.info("Published email-notification: to={}", to);
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize email notification for {}", to, e);
        }
    }
}