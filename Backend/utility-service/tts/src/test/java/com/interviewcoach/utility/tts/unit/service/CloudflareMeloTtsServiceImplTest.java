package com.interviewcoach.utility.tts.unit.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.interviewcoach.utility.tts.service.impl.CloudflareMeloTtsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class CloudflareMeloTtsServiceImplTest {

    @Mock
    private WebClient webClient;

    private ObjectMapper objectMapper;

    private CloudflareMeloTtsServiceImpl cloudflareMeloTtsService;

    @BeforeEach
    @SuppressWarnings("null")
    void setUp() {
        objectMapper = new ObjectMapper();
        cloudflareMeloTtsService = new CloudflareMeloTtsServiceImpl(webClient, objectMapper);
        ReflectionTestUtils.setField(cloudflareMeloTtsService, "apiBaseUrl", "http://test.example.com");
        ReflectionTestUtils.setField(cloudflareMeloTtsService, "apiToken", "test-token");
    }

    @Test
    void getAvailableVoices_shouldReturnVoices() {
        String[] voices = cloudflareMeloTtsService.getAvailableVoices();

        assertThat(voices).isNotEmpty();
        assertThat(voices).contains("en", "es", "fr");
    }

    @Test
    void transcribeAudio_shouldThrowUnsupportedOperationException() {
        assertThatThrownBy(() -> cloudflareMeloTtsService.transcribeAudio(new byte[]{0x01}))
            .isInstanceOf(UnsupportedOperationException.class)
            .hasMessageContaining("Cloudflare MeloTTS does not support speech-to-text transcription");
    }
}
