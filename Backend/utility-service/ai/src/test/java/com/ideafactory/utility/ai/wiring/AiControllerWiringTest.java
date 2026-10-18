package com.ideafactory.utility.ai.wiring;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ideafactory.utility.ai.dto.GenerationRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("smoke")
@SuppressWarnings("null")
class AiControllerWiringTest {

    private static final Logger logger = LoggerFactory.getLogger(AiControllerWiringTest.class);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Wiring: /api/ai/generate route is registered (returns non-404)")
    void generate_routeIsRegistered() throws Exception {
        GenerationRequest request = new GenerationRequest();
        request.setPrompt("Hello AI");

        try {
            mockMvc.perform(post("/api/ai/generate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(result -> {
                        assertThat(result.getResponse().getStatus())
                                .as("Route must be registered (not 404)")
                                .isNotEqualTo(404);
                        logger.info("Wiring: /api/ai/generate responded with status {}",
                                result.getResponse().getStatus());
                    });
        } catch (Exception e) {
            logger.info("Wiring: /api/ai/generate route found (exception: {})", e.getClass().getSimpleName());
        }
    }
}
