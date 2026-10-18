package com.interviewcoach.utility.tts.unit.service;

import com.interviewcoach.utility.tts.service.impl.KorokoTtsServiceImpl;
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
class KorokoTtsServiceImplTest {

    @Mock
    private WebClient webClient;

    private KorokoTtsServiceImpl korokoTtsService;

    @BeforeEach
    @SuppressWarnings("null")
    void setUp() {
        korokoTtsService = new KorokoTtsServiceImpl(webClient, new com.fasterxml.jackson.databind.ObjectMapper());
        ReflectionTestUtils.setField(korokoTtsService, "baseUrl", "http://localhost:8880");
        ReflectionTestUtils.setField(korokoTtsService, "speed", 1.0);
        ReflectionTestUtils.setField(korokoTtsService, "responseFormat", "mp3");
    }

    @Test
    void getAvailableVoices_shouldReturnDefaultVoices_whenApiFails() throws Exception {
        String[] voices = korokoTtsService.getAvailableVoices();

        assertThat(voices).isNotEmpty();
        assertThat(voices).contains("af_bella", "af_sarah", "am_adam");
    }

    @Test
    void getAvailableVoices_shouldReturnVoices_whenApiSucceeds() throws Exception {
        String[] voices = korokoTtsService.getAvailableVoices();

        assertThat(voices).isNotEmpty();
        assertThat(voices).hasSize(15);
        assertThat(voices[0]).isEqualTo("af_bella");
    }

    @Test
    void transcribeAudio_shouldThrowUnsupportedOperationException() {
        assertThatThrownBy(() -> korokoTtsService.transcribeAudio(new byte[]{0x01}))
            .isInstanceOf(UnsupportedOperationException.class)
            .hasMessageContaining("Kokoro TTS does not support speech-to-text transcription");
    }
}
