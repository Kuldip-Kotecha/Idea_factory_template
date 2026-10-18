package com.ideafactory.utility.tts.unit.controller;

import com.ideafactory.utility.tts.controller.AudioController;
import com.ideafactory.utility.tts.dto.SpeakRequest;
import com.ideafactory.utility.tts.service.TtsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SuppressWarnings("null")
class AudioControllerTest {

    private final TtsService ttsService = mock(TtsService.class);
    private final AudioController audioController = new AudioController(ttsService);
    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(audioController).build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void speak_shouldReturnAudioBytes() throws Exception {
        byte[] fakeAudio = new byte[] { 0x52, 0x49, 0x46, 0x46 };
        when(ttsService.generateAudio(eq("Hello world"), eq("professional")))
                .thenReturn(fakeAudio);

        SpeakRequest request = new SpeakRequest();
        request.setText("Hello world");
        request.setTone("professional");

        mockMvc.perform(post("/api/audio/speak")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"speech.wav\""))
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM));
    }

    @Test
    void speak_shouldReturn204WhenAudioEmpty() throws Exception {
        when(ttsService.generateAudio(eq("test"), eq("")))
                .thenReturn(new byte[0]);

        SpeakRequest request = new SpeakRequest();
        request.setText("test");
        request.setTone("");

        mockMvc.perform(post("/api/audio/speak")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
    }

    @Test
    void speak_shouldReturn204WhenAudioNull() throws Exception {
        when(ttsService.generateAudio(eq("test"), eq("calm")))
                .thenReturn(null);

        SpeakRequest request = new SpeakRequest();
        request.setText("test");
        request.setTone("calm");

        mockMvc.perform(post("/api/audio/speak")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
    }
}
