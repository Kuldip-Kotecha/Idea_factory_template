package com.ideafactory.core.wiring;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * Wiring test — verifies Spring context loads and core controller routes are registered.
 * Runs at build time with H2 in-memory DB (application-smoke.yml).
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("smoke")
class CoreServiceWiringTest {

    private static final Logger logger = LoggerFactory.getLogger(CoreServiceWiringTest.class);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Wiring: /api/core/health route is registered (returns 200 OK)")
    void healthEndpoint_routeIsRegistered() throws Exception {
        mockMvc.perform(get("/api/core/health"))
                .andDo(result -> {
                    assertThat(result.getResponse().getStatus())
                            .as("Route /api/core/health must be 200 OK")
                            .isEqualTo(200);
                    logger.info("Wiring: /api/core/health responded with status {}", result.getResponse().getStatus());
                });
    }

    @Test
    @DisplayName("Wiring: /api/core/items route is registered (returns non-404)")
    void itemsEndpoint_routeIsRegistered() throws Exception {
        mockMvc.perform(get("/api/core/items"))
                .andDo(result -> {
                    assertThat(result.getResponse().getStatus())
                            .as("Route /api/core/items must be registered (not 404)")
                            .isNotEqualTo(404);
                    logger.info("Wiring: /api/core/items responded with status {}", result.getResponse().getStatus());
                });
    }

    @Test
    @DisplayName("Wiring: Core service beans are properly loaded")
    void beans_areLoaded() {
        assertThat(mockMvc).isNotNull();
        logger.info("Wiring: Core service beans loaded successfully");
    }
}
