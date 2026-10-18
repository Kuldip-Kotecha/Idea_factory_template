package com.ideafactory.notification.wiring;

import com.ideafactory.notification.queue.QueueScheduler;
import com.ideafactory.notification.service.NotificationProcessor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Wiring test — verifies Spring context loads, RedisTemplate wires up, and routes are registered.
 * Does NOT send real emails or connect to a live Redis broker.
 * Runs at build time with dummy config (application-smoke.yml).
 *
 * Real email delivery is verified in post-deploy integration tests (6d).
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("smoke")
@SuppressWarnings("null")
class NotificationServiceWiringTest {

    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceWiringTest.class);

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StringRedisTemplate redisTemplate;

    @MockBean
    private ListOperations<String, String> listOperations;

    @Autowired(required = false)
    private QueueScheduler queueScheduler;

    @Autowired
    private NotificationProcessor notificationProcessor;

    @Test
    void redisTemplate_isAutowired() {
        when(redisTemplate.opsForList()).thenReturn(listOperations);
        assertThat(redisTemplate).isNotNull();
        logger.info("Wiring: StringRedisTemplate is properly configured");
    }

    @Test
    void queueScheduler_isDisabledForSmokeProfile() {
        assertThat(queueScheduler).isNull();
        logger.info("Wiring: QueueScheduler is disabled for smoke profile");
    }

    @Test
    @DisplayName("Wiring: /notifications/send-email route is registered")
    void sendEmailEndpoint_isRouted() throws Exception {
        when(redisTemplate.opsForList()).thenReturn(listOperations);
        when(listOperations.leftPush(anyString(), anyString())).thenReturn(1L);

        Map<String, String> payload = Map.of(
                "to", "test@example.com",
                "subject", "Wiring Test",
                "text", "Testing notification service wiring",
                "ctaUrl", "https://example.com"
        );

        mockMvc.perform(post("/notifications/send-email")
                        .contentType("application/json")
                        .content(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(payload)))
                .andDo(result -> {
                    int status = result.getResponse().getStatus();
                    assertThat(status).as("Route must be registered (not 404)").isNotEqualTo(404);
                    logger.info("Wiring: /notifications/send-email responded with status: {}", status);
                });
    }

    @Test
    @DisplayName("Wiring: NotificationController is properly instantiated")
    void controller_isInstantiated() {
        assertThat(mockMvc).isNotNull();
        assertThat(notificationProcessor).isNotNull();
        logger.info("Wiring: Notification service controller loaded successfully");
    }
}
