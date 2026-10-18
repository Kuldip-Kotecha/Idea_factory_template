package com.ideafactory.utility.tts.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ideafactory.utility.tts.service.TtsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
@SuppressWarnings("null")
public class KorokoTtsServiceImpl implements TtsService {

    private static final Logger logger = LoggerFactory.getLogger(KorokoTtsServiceImpl.class);

    private static final String KOKORO_API_URL = "/v1/audio/speech";
    private static final String VOICES_API_URL = "/v1/audio/voices";

    private static final List<String> AVAILABLE_VOICES = List.of(
        "af_bella", "af_sarah", "am_adam", "am_michael",
        "bf_emma", "bf_isabella", "bm_george", "bm_lewis",
        "af_nicole", "af_sky", "af_heart", "af_classic",
        "bf_university", "bm_fable", "bm_leo"
    );

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${koroko.base-url:http://kokoro-tts:8880}")
    private String baseUrl;

    @Value("${koroko.speed:1.0}")
    private double speed;

    @Value("${koroko.response-format:mp3}")
    private String responseFormat;

    public KorokoTtsServiceImpl(WebClient webClient, ObjectMapper objectMapper) {
        this.webClient = webClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public byte[] generateAudio(String text, String voice) {
        String resolvedVoice = resolveVoice(voice);

        logger.info("Generating Kokoro TTS audio with voice: {}, speed: {}", resolvedVoice, speed);

        Map<String, Object> requestBody = Map.of(
            "model", "kokoro",
            "input", text,
            "voice", resolvedVoice,
            "response_format", responseFormat,
            "speed", speed
        );

        try {
            byte[] audioData = webClient.post()
                .uri(baseUrl + KOKORO_API_URL)
                .header("Authorization", "Bearer not-needed")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(byte[].class)
                .timeout(Duration.ofSeconds(120))
                .block();

            if (audioData == null || audioData.length == 0) {
                throw new RuntimeException("Kokoro TTS returned empty audio");
            }
            logger.info("Successfully generated audio: {} bytes", audioData.length);
            return audioData;
        } catch (Exception e) {
            logger.error("Kokoro TTS generation failed: {}", e.getMessage(), e);
            throw new RuntimeException("Kokoro TTS generation failed: " + e.getMessage(), e);
        }
    }

    @Override
    public String transcribeAudio(byte[] audioData) {
        throw new UnsupportedOperationException("Kokoro TTS does not support speech-to-text transcription");
    }

    @Override
    public String[] getAvailableVoices() {
        try {
            String response = webClient.get()
                .uri(baseUrl + VOICES_API_URL)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(10))
                .block();

            JsonNode root = objectMapper.readTree(response);
            JsonNode voices = root.get("voices");
            String[] voiceArray = new String[voices.size()];
            for (int i = 0; i < voices.size(); i++) {
                voiceArray[i] = voices.get(i).asText();
            }
            return voiceArray;
        } catch (Exception e) {
            logger.warn("Failed to fetch voices from Kokoro service, using defaults: {}", e.getMessage());
            return AVAILABLE_VOICES.toArray(new String[0]);
        }
    }

    private String resolveVoice(String voice) {
        if (voice == null || voice.isBlank()) {
            return "af_bella";
        }
        for (String available : AVAILABLE_VOICES) {
            if (available.equalsIgnoreCase(voice)) {
                return available;
            }
        }
        return "af_bella";
    }
}