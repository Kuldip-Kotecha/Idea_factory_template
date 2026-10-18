package com.ideafactory.media.queue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ideafactory.media.event.UserDeletedEvent;
import com.ideafactory.media.service.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class QueueScheduler {

    private static final Logger logger = LoggerFactory.getLogger(QueueScheduler.class);

    private static final String USER_DELETED_MEDIA_QUEUE = "user-deleted-media";
    private static final String MEDIA_PROCESSING_QUEUE = "media-processing";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final StorageService storageService;

    public QueueScheduler(StringRedisTemplate redisTemplate, ObjectMapper objectMapper, StorageService storageService) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.storageService = storageService;
    }

    @Scheduled(fixedDelayString = "${app.queue.poll-interval-ms:1000}")
    public void pollUserDeletedQueue() {
        try {
            String message = redisTemplate.opsForList().rightPop(USER_DELETED_MEDIA_QUEUE);
            if (message != null) {
                logger.debug("Polled user-deleted-media message: {}", message);
                processUserDeleted(message);
            }
        } catch (Exception e) {
            logger.error("Error polling user-deleted-media queue", e);
        }
    }

    @Scheduled(fixedDelayString = "${app.queue.poll-interval-ms:1000}")
    public void pollMediaProcessingQueue() {
        try {
            String message = redisTemplate.opsForList().rightPop(MEDIA_PROCESSING_QUEUE);
            if (message != null) {
                logger.debug("Polled media-processing message: {}", message);
                processMedia(message);
            }
        } catch (Exception e) {
            logger.error("Error polling media-processing queue", e);
        }
    }

    private void processUserDeleted(String jsonMessage) {
        try {
            UserDeletedEvent event = objectMapper.readValue(jsonMessage, UserDeletedEvent.class);

            if (event.getUserId() == null) {
                logger.warn("Received UserDeletedEvent with null userId, ignoring");
                return;
            }

            logger.info("Processing UserDeletedEvent for userId={}", event.getUserId());

            storageService.archiveFolder(String.valueOf(event.getUserId()));
            storageService.archiveFolder("resumes/" + event.getUserId());
            logger.info("Archived media and resumes for userId={}", event.getUserId());

        } catch (Exception e) {
            logger.error("Failed to process user-deleted message: {}", jsonMessage, e);
        }
    }

    private void processMedia(String jsonMessage) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> message = objectMapper.readValue(jsonMessage, Map.class);

            Long interviewId = message.get("interviewId") != null
                    ? ((Number) message.get("interviewId")).longValue()
                    : null;
            String filename = (String) message.get("filename");
            String action = (String) message.get("action");

            logger.info("Processing media for interview={}, file={}, action={}", interviewId, filename, action);

            if ("ANALYZE_PROSODY".equals(action)) {
                logger.info("Prosody analysis queued for interview {}. Integrate with AI service for actual processing.", interviewId);
            }

            logger.info("Media processing completed for interview={}, file={}", interviewId, filename);
        } catch (Exception e) {
            logger.error("Failed to process media message: {}", jsonMessage, e);
        }
    }
}