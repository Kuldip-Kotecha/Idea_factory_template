package com.ideafactory.utility.tts.controller;

import com.ideafactory.utility.tts.dto.SpeakRequest;
import com.ideafactory.utility.tts.service.TtsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/audio")
public class AudioController {

    private static final Logger logger = LoggerFactory.getLogger(AudioController.class);
    private final TtsService ttsService;

    public AudioController(TtsService ttsService) {
        this.ttsService = ttsService;
    }

    @PostMapping("/speak")
    public ResponseEntity<byte[]> speak(@RequestBody SpeakRequest request) {
        try {
            if (request.getText() == null || request.getText().isBlank()) {
                return ResponseEntity.noContent().build();
            }
            byte[] audioData = ttsService.generateAudio(request.getText(), request.getTone());
            if (audioData == null || audioData.length == 0) {
                return ResponseEntity.noContent().build();
            }
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDisposition(ContentDisposition.builder("attachment").filename("speech.wav").build());
            return new ResponseEntity<>(audioData, headers, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("TTS generation failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/transcribe")
    public ResponseEntity<String> transcribeAudio(@RequestParam("file") MultipartFile file) {
        try {
            String transcription = ttsService.transcribeAudio(file.getBytes());
            return ResponseEntity.ok(transcription);
        } catch (Exception e) {
            logger.error("Transcription failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Transcription failed: " + e.getMessage());
        }
    }

    @GetMapping("/voices")
    public ResponseEntity<String[]> getAvailableVoices() {
        return ResponseEntity.ok(ttsService.getAvailableVoices());
    }
}