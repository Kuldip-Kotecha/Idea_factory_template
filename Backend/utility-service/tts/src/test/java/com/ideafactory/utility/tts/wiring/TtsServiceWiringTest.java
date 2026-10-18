package com.ideafactory.utility.tts.wiring;

import com.ideafactory.utility.tts.controller.AudioController;
import com.ideafactory.utility.tts.service.TtsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Wiring test — verifies TTS controller routes are registered.
 * Does NOT call any real TTS API.
 *
 * Real TTS API health is verified in the post-deploy smoke step (Xe) in the pipeline.
 */
class TtsServiceWiringTest {

    private static final Logger logger = LoggerFactory.getLogger(TtsServiceWiringTest.class);

    private final TtsService ttsService = mock(TtsService.class);
    private final AudioController audioController = new AudioController(ttsService);
    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(audioController).build();

    @Test
    @DisplayName("Wiring: /api/audio/voices returns list of available voices")
    void getAvailableVoices_returnsVoiceList() throws Exception {
        when(ttsService.getAvailableVoices()).thenReturn(new String[]{
            "a0e99841-438c-4a64-b679-ae501e7d6091",
            "40104aff-a015-4da1-9912-f6227943806d",
            "86ae44c7-97a1-46ce-89d9-0c5eea9da456",
            "c2ac25f9-eb4b-4de5-9e5b-a41e8185ac93"
        });

        MvcResult result = mockMvc.perform(get("/api/audio/voices"))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        assertThat(response).isNotBlank();

        var voices = new com.fasterxml.jackson.databind.ObjectMapper().readValue(response, String[].class);
        assertThat(voices).isNotEmpty();
        assertThat(voices.length).isEqualTo(4);
        assertThat(voices).contains(
            "a0e99841-438c-4a64-b679-ae501e7d6091",
            "40104aff-a015-4da1-9912-f6227943806d",
            "86ae44c7-97a1-46ce-89d9-0c5eea9da456",
            "c2ac25f9-eb4b-4de5-9e5b-a41e8185ac93"
        );

        logger.info("Wiring: /api/audio/voices returned {} voices: {}", voices.length, (Object) voices);
    }

    @Test
    @DisplayName("Wiring: TtsService and AudioController are properly loaded")
    void beans_areLoaded() {
        assertThat(mockMvc).isNotNull();
        logger.info("Wiring: TTS service beans loaded successfully");
    }
}
