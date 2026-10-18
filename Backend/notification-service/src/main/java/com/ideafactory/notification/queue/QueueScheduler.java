package com.ideafactory.notification.queue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ideafactory.notification.service.NotificationProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.queue.enabled", havingValue = "true", matchIfMissing = true)
public class QueueScheduler {

    private static final Logger logger = LoggerFactory.getLogger(QueueScheduler.class);

    private static final String EMAIL_NOTIFICATIONS_QUEUE = "email-notifications";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final NotificationProcessor notificationProcessor;

    public QueueScheduler(StringRedisTemplate redisTemplate, ObjectMapper objectMapper,
            NotificationProcessor notificationProcessor) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.notificationProcessor = notificationProcessor;
    }

    @Scheduled(fixedDelayString = "${app.queue.poll-interval-ms:1000}")
    public void pollEmailNotificationsQueue() {
        try {
            String message = redisTemplate.opsForList().rightPop(EMAIL_NOTIFICATIONS_QUEUE);
            if (message != null) {
                logger.debug("Polled email-notification message");
                processEmailNotification(message);
            }
        } catch (Exception e) {
            logger.error("Error polling email-notifications queue", e);
        }
    }

    private void processEmailNotification(String jsonMessage) {
        try {
            @SuppressWarnings("unchecked")
            java.util.Map<String, String> payload = objectMapper.readValue(jsonMessage, java.util.Map.class);

            String to = payload.get("to");
            String subject = payload.get("subject");
            String text = payload.get("text");
            String ctaUrl = payload.get("ctaUrl");

            if (to != null) {
                notificationProcessor.sendHtmlMessage(to, subject, text, ctaUrl);
            } else {
                logger.warn("Received email notification without 'to' field: {}", payload);
            }
        } catch (Exception e) {
            logger.error("Failed to process email notification: {}", jsonMessage, e);
        }
    }
}