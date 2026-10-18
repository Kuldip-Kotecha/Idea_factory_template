package com.ideafactory.utility.ai.service.impl;

import com.ideafactory.utility.ai.dto.GenerationRequest;
import com.ideafactory.utility.ai.service.GenAiService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
@Primary
@SuppressWarnings("null")
public class HuggingFaceGenAiServiceImpl implements GenAiService {

    private static final Logger logger = LoggerFactory.getLogger(HuggingFaceGenAiServiceImpl.class);
    private static final String INFERENCE_API_PATH = "/v1/chat/completions";
    private static final java.time.Duration HUGGINGFACE_TIMEOUT = java.time.Duration.ofSeconds(60);

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final WebClient webClient;

    @Value("${ai.api-key}")
    private String apiKey;

    @Value("${ai.model}")
    private String modelName;

    @Value("${ai.base-url}")
    private String apiBaseUrl;

    @Value("${huggingface.temperature:0.7}")
    private double temperature;

    @Value("${huggingface.max_tokens:1024}")
    private int maxTokens;

    public HuggingFaceGenAiServiceImpl(WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public String runSmokeCheck() {
        String response = callHuggingFaceApi("Reply with exactly OK.", 24);
        if (response == null || response.isBlank()) {
            throw new RuntimeException("HuggingFace smoke check returned an empty response");
        }
        return response.trim();
    }

    @Override
    public String generate(GenerationRequest request) {
        String prompt = request.getPrompt() != null ? request.getPrompt() : "Hello";
        int requestedTokens = request.getMaxTokens() != null ? request.getMaxTokens() : maxTokens;
        double reqTemp = request.getTemperature() != null ? request.getTemperature() : temperature;

        List<Map<String, Object>> messages = new ArrayList<>();
        if (request.getSystemPrompt() != null && !request.getSystemPrompt().isBlank()) {
            messages.add(Map.of("role", "system", "content", request.getSystemPrompt()));
        }
        messages.add(Map.of("role", "user", "content", prompt));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", modelName);
        requestBody.put("messages", messages);
        requestBody.put("temperature", reqTemp);
        requestBody.put("max_tokens", resolveMaxTokens(requestedTokens));
        requestBody.put("stream", false);

        logger.debug("HuggingFace: Calling model={} prompt_length={}", modelName, prompt.length());

        try {
            String responseBody = webClient.post()
                .uri(apiBaseUrl + INFERENCE_API_PATH)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(HUGGINGFACE_TIMEOUT)
                .block();

            return parseHuggingFaceResponse(responseBody);
        } catch (WebClientResponseException e) {
            int statusCode = e.getStatusCode().value();
            String responseBody = e.getResponseBodyAsString();
            logger.error("HuggingFace API call failed with status {}: {}", statusCode, responseBody);

            if (statusCode == 401 || statusCode == 403) {
                throw new RuntimeException("HuggingFace API authentication failed (HTTP " + statusCode + "). Please verify your AI_API_KEY is valid.", e);
            } else if (statusCode == 422) {
                throw new RuntimeException("HuggingFace API validation error (HTTP 422).", e);
            } else if (statusCode >= 500) {
                throw new RuntimeException("HuggingFace API upstream failure (HTTP " + statusCode + ").", e);
            }
            throw new RuntimeException("HuggingFace API call failed with HTTP " + statusCode + ": " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("HuggingFace API call failed: {}", e.getMessage(), e);
            throw new RuntimeException("HuggingFace API call failed: " + e.getMessage(), e);
        }
    }

    private String callHuggingFaceApi(String promptText, int requestedMaxTokens) {
        GenerationRequest req = new GenerationRequest();
        req.setPrompt(promptText);
        req.setMaxTokens(requestedMaxTokens);
        return generate(req);
    }

    private String parseHuggingFaceResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);

            JsonNode errorNode = root.get("error");
            if (errorNode != null && !errorNode.isNull()) {
                String errorMsg = errorNode.has("message") ? errorNode.get("message").asText() : errorNode.asText();
                logger.error("HuggingFace returned error: {}", errorMsg);
                throw new RuntimeException("HuggingFace API error: " + errorMsg);
            }

            JsonNode choices = root.get("choices");
            if (choices != null && choices.isArray() && choices.size() > 0) {
                JsonNode message = choices.get(0).get("message");
                if (message != null && message.has("content")) {
                    return message.get("content").asText();
                }
            }

            logger.error("HuggingFace response missing expected structure: {}", responseBody);
            throw new RuntimeException("Unexpected HuggingFace response format");
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception e) {
            logger.error("Failed to parse HuggingFace response", e);
            throw new RuntimeException("Failed to parse HuggingFace response", e);
        }
    }

    private int resolveMaxTokens(int requestedMaxTokens) {
        if (requestedMaxTokens <= 0) {
            return maxTokens;
        }
        return Math.min(maxTokens, requestedMaxTokens);
    }

    @Override
    public String transcribeAudio(byte[] audioData) {
        logger.info("Transcribing audio using Hugging Face Whisper API...");
        try {
            String whisperUrl = "https://api-inference.huggingface.co/models/openai/whisper-large-v3";
            
            byte[] responseData = webClient.post()
                .uri(whisperUrl)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .contentType(MediaType.parseMediaType("audio/webm"))
                .bodyValue(audioData)
                .retrieve()
                .bodyToMono(byte[].class)
                .timeout(HUGGINGFACE_TIMEOUT)
                .block();

            if (responseData == null || responseData.length == 0) {
                throw new RuntimeException("HuggingFace Whisper returned empty response");
            }

            String responseStr = new String(responseData, StandardCharsets.UTF_8);
            logger.debug("HuggingFace Whisper response: {}", responseStr);
            
            JsonNode root = objectMapper.readTree(responseStr);
            if (root.has("text")) {
                String text = root.get("text").asText();
                logger.info("Successfully transcribed audio with HF: length={}", text.length());
                return text;
            } else if (root.has("error")) {
                throw new RuntimeException("HuggingFace STT error: " + root.get("error").asText());
            }

            throw new RuntimeException("Unexpected HuggingFace Whisper response format: " + responseStr);
        } catch (Exception e) {
            logger.error("HuggingFace Whisper transcription failed: {}", e.getMessage(), e);
            throw new RuntimeException("Transcription failed: " + e.getMessage(), e);
        }
    }
}