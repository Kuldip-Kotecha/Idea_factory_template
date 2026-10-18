package com.interviewcoach.utility.ai.unit.controller;

import com.interviewcoach.utility.ai.dto.GenerationRequest;
import com.interviewcoach.utility.ai.service.GenAiService;
import com.interviewcoach.utility.ai.controller.AiController;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = AiController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
    com.interviewcoach.utility.security.JwtAuthenticationFilter.class,
    com.interviewcoach.utility.filter.RateLimitingFilter.class
}))
@AutoConfigureMockMvc(addFilters = false)
@SuppressWarnings("null")
class AiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GenAiService genAiService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void smokeCheck_shouldReturnProviderReply() throws Exception {
        when(genAiService.runSmokeCheck()).thenReturn("OK");

        mockMvc.perform(get("/api/ai/smoke-check"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("UP"))
            .andExpect(jsonPath("$.providerReply").value("OK"));

        verify(genAiService).runSmokeCheck();
    }

    @Test
    void generate_shouldReturnAiResponse() throws Exception {
        when(genAiService.generate(any(GenerationRequest.class)))
            .thenReturn("Hello! How can I assist your application today?");

        GenerationRequest request = new GenerationRequest();
        request.setPrompt("Hello");

        mockMvc.perform(post("/api/ai/generate")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").value("Hello! How can I assist your application today?"));

        verify(genAiService).generate(any(GenerationRequest.class));
    }

    @Test
    void transcribe_shouldReturnTranscript() throws Exception {
        when(genAiService.transcribeAudio(any())).thenReturn("Test speech transcription");

        MockMultipartFile file = new MockMultipartFile("file", "audio.webm", "audio/webm", new byte[]{1, 2, 3});

        mockMvc.perform(multipart("/api/ai/transcribe").file(file))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.text").value("Test speech transcription"));
    }
}