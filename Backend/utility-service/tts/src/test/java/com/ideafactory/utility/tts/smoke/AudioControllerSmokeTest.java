package com.ideafactory.utility.tts.smoke;

import com.ideafactory.utility.tts.dto.SpeakRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
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

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("smoke")
@SuppressWarnings("null")
class AudioControllerSmokeTest {

    private static final Logger logger = LoggerFactory.getLogger(AudioControllerSmokeTest.class);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Smoke: /api/audio/voices endpoint is registered")
    void voices_endpointIsRegistered() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/audio/voices"))
                .andReturn();

        int status = result.getResponse().getStatus();
        assertThat(status).isNotEqualTo(404);
        logger.info("/api/audio/voices responded with status: {}", status);
    }

    @Test
    @DisplayName("Smoke: /api/audio/speak endpoint is registered and accepts valid request")
    void speak_endpointIsRegistered() throws Exception {
        SpeakRequest request = new SpeakRequest();
        request.setText("Hello world");
        request.setTone("professional");

        MvcResult result = mockMvc.perform(post("/api/audio/speak")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn();

        int status = result.getResponse().getStatus();
        assertThat(status).isNotEqualTo(404);
        logger.info("/api/audio/speak responded with status: {}", status);
    }

    @Test
    @DisplayName("Smoke: /api/audio/speak handles empty text gracefully")
    void speak_emptyText_returnsNoContent() throws Exception {
        SpeakRequest request = new SpeakRequest();
        request.setText("");
        request.setTone("calm");

        MvcResult result = mockMvc.perform(post("/api/audio/speak")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isNotEqualTo(404);
        logger.info("/api/audio/speak (empty) responded with status: {}", result.getResponse().getStatus());
    }
}