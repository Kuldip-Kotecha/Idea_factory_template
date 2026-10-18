package com.interviewcoach.utility.tts.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.interviewcoach.utility.tts.service.TtsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
@Primary
@SuppressWarnings("null")
public class CloudflareMeloTtsServiceImpl implements TtsService {

    private static final Logger logger = LoggerFactory.getLogger(CloudflareMeloTtsServiceImpl.class);

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${AI_TTS_API_KEY:}")
    private String apiToken;

    @Value("${AI_TTS_API_BASE_URL:}")
    private String apiBaseUrl;

    @Value("${AI_TTS_MODEL:}")
    private String modelName;

    private static final List<String> AVAILABLE_VOICES = List.of("en", "es", "fr", "zh", "jp", "kr");

    public CloudflareMeloTtsServiceImpl(WebClient webClient, ObjectMapper objectMapper) {
        this.webClient = webClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public byte[] generateAudio(String text, String voice) {
        String resolvedVoice = resolveVoice(voice);

        logger.info("Generating Cloudflare MeloTTS audio with voice/lang: {}", resolvedVoice);

        Map<String, Object> requestBody = Map.of(
            "prompt", text,
            "lang", resolvedVoice
        );

        try {
            byte[] responseData = webClient.post()
                .uri(apiBaseUrl + modelName)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(byte[].class)
                .timeout(Duration.ofSeconds(60))
                .block();

            if (responseData == null || responseData.length == 0) {
                throw new RuntimeException("Cloudflare MeloTTS returned empty response");
            }

            // Attempt to parse as JSON first
            try {
                String responseStr = new String(responseData, java.nio.charset.StandardCharsets.UTF_8);
                if (responseStr.trim().startsWith("{")) {
                    JsonNode root = objectMapper.readTree(responseStr);
                    if (root.has("result") && root.get("result").has("audio")) {
                        JsonNode audioNode = root.get("result").get("audio");
                        if (audioNode.isArray()) {
                            // Convert array of numbers to bytes
                            byte[] audioBytes = new byte[audioNode.size()];
                            for (int i = 0; i < audioNode.size(); i++) {
                                audioBytes[i] = (byte) audioNode.get(i).asInt();
                            }
                            logger.info("Successfully generated audio from Cloudflare MeloTTS (array format): {} bytes", audioBytes.length);
                            return audioBytes;
                        } else if (audioNode.isTextual()) {
                            // Base64 encoded MP3 audio
                            byte[] decodedBytes = Base64.getDecoder().decode(audioNode.asText());
                            logger.info("Successfully generated audio from Cloudflare MeloTTS (base64 format): {} bytes", decodedBytes.length);
                            return decodedBytes;
                        }
                    } else if (root.has("success") && !root.get("success").asBoolean()) {
                        throw new RuntimeException("Cloudflare API error: " + root.get("errors").toString());
                    }
                }
            } catch (com.fasterxml.jackson.core.JsonProcessingException ignored) {
                // Not a valid JSON, fall through and return raw binary audio
            }

            // Return raw binary audio
            logger.info("Successfully generated audio from Cloudflare MeloTTS (binary format): {} bytes", responseData.length);
            return responseData;
        } catch (Exception e) {
            logger.error("Cloudflare MeloTTS generation failed: {}", e.getMessage(), e);
            throw new RuntimeException("TTS generation failed: " + e.getMessage(), e);
        }
    }

    @Override
    public String transcribeAudio(byte[] audioData) {
        throw new UnsupportedOperationException("Cloudflare MeloTTS does not support speech-to-text transcription");
    }

    @Override
    public String[] getAvailableVoices() {
        return AVAILABLE_VOICES.toArray(new String[0]);
    }

    private String resolveVoice(String voice) {
        if (voice == null || voice.isBlank()) {
            return "en";
        }
        for (String available : AVAILABLE_VOICES) {
            if (available.equalsIgnoreCase(voice)) {
                return available;
            }
        }
        return "en";
    }
}
