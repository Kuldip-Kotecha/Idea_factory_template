package com.ideafactory.utility.ai.controller;

import com.ideafactory.utility.ai.dto.GenerationRequest;
import com.ideafactory.utility.ai.service.GenAiService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private static final Logger logger = LoggerFactory.getLogger(AiController.class);

    private final GenAiService genAiService;

    public AiController(GenAiService genAiService) {
        this.genAiService = genAiService;
    }

    @GetMapping("/smoke-check")
    public Map<String, String> smokeCheck() {
        return Map.of(
            "status", "UP",
            "providerReply", genAiService.runSmokeCheck());
    }

    @PostMapping("/generate")
    public Map<String, String> generate(@Valid @RequestBody GenerationRequest request) {
        String result = genAiService.generate(request);
        return Map.of("content", result);
    }

    @PostMapping(value = "/transcribe", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, String> transcribeAudio(@RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        try {
            String text = genAiService.transcribeAudio(file.getBytes());
            return Map.of("text", text);
        } catch (Exception e) {
            logger.error("STT Transcription failed: {}", e.getMessage(), e);
            throw new RuntimeException("Transcription failed: " + e.getMessage());
        }
    }
}