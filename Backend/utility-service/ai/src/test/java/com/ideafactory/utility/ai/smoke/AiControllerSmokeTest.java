package com.ideafactory.utility.ai.smoke;

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
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("smoke")
@SuppressWarnings("null")
class AiControllerSmokeTest {

    private static final Logger logger = LoggerFactory.getLogger(AiControllerSmokeTest.class);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Smoke: /api/ai/smoke-check endpoint returns UP")
    void smokeCheck_returnsUp() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/ai/smoke-check"))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        logger.info("Smoke check response: {}", response);
        assertThat(response).contains("UP");
    }

    @Test
    @DisplayName("Smoke: /api/ai/generate route is registered and accepts valid request")
    void generate_routeWorks() throws Exception {
        GenerationRequest request = new GenerationRequest();
        request.setPrompt("Hello AI");

        MvcResult result = mockMvc.perform(post("/api/ai/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn();

        int httpStatus = result.getResponse().getStatus();
        logger.info("Smoke: /api/ai/generate returned HTTP {}", httpStatus);
        assertThat(httpStatus).isNotEqualTo(404);
    }
}