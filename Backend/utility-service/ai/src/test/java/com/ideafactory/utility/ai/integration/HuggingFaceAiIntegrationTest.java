package com.ideafactory.utility.ai.integration;

import com.ideafactory.utility.ai.dto.GenerationRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("integration-test")
@EnabledIfEnvironmentVariable(named = "AI_API_KEY", matches = ".+")
@SuppressWarnings("null")
class HuggingFaceAiIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(HuggingFaceAiIntegrationTest.class);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${ai.model:}")
    private String aiModel;

    @BeforeEach
    void setUp() {
        logger.info("Running HuggingFace integration test with model: {}", aiModel);
    }

    @Test
    @DisplayName("Should successfully generate text via HuggingFace API")
    void testGenerate_RealApiCall() throws Exception {
        GenerationRequest request = new GenerationRequest();
        request.setPrompt("What is the capital of France?");

        MvcResult result = mockMvc.perform(post("/api/ai/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        logger.info("AI generated response: {}", response);

        assertThat(response).contains("Paris");
    }
}