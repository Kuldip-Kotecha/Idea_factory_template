package com.interviewcoach.media.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.interviewcoach.media.service.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/media")
@SuppressWarnings("null")
public class MediaController {

    private static final Logger logger = LoggerFactory.getLogger(MediaController.class);
    private static final String MEDIA_PROCESSING_QUEUE = "media-processing";

    private final StorageService storageService;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public MediaController(StorageService storageService, StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.storageService = storageService;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/save")
    public String saveAudioChunk(@RequestParam("userId") Long userId,
                                  @RequestParam("interviewId") Long interviewId,
                                  @RequestParam("messageId") Long messageId,
                                  @RequestBody byte[] audioData) {
        try {
            String subDir = userId + "/" + interviewId;
            String fileName = messageId.toString();
            String savedPath = storageService.saveFile(audioData, subDir, fileName);
            return savedPath;
        } catch (IOException e) {
            throw new RuntimeException("Failed to save audio chunk: " + e.getMessage(), e);
        }
    }

    @PostMapping("/merge")
    public Map<String, String> mergeAudio(@RequestBody MergeRequest request) {
        if (request.getInterviewId() == null) {
            logger.error("mergeAudio called with null interviewId");
            throw new IllegalArgumentException("interviewId is required");
        }
        if (request.getAllPaths() == null && request.getUserPaths() == null) {
            logger.error("mergeAudio called with null paths for interviewId: {}", request.getInterviewId());
            throw new IllegalArgumentException("At least one of allPaths or userPaths must be provided");
        }

        logger.info("Received merge request for interviewId: {}, allPaths count: {}, userPaths count: {}",
                request.getInterviewId(),
                request.getAllPaths() != null ? request.getAllPaths().size() : 0,
                request.getUserPaths() != null ? request.getUserPaths().size() : 0);

        if (request.getAllPaths() != null && !request.getAllPaths().isEmpty()) {
            logger.debug("allPaths: {}", request.getAllPaths());
        }
        if (request.getUserPaths() != null && !request.getUserPaths().isEmpty()) {
            logger.debug("userPaths: {}", request.getUserPaths());
        }

        try {
            String subDir = "merged";

            String fullPath = "";
            String userPath = "";

            if (!request.getAllPaths().isEmpty()) {
                String fullFileName = "interview_" + request.getInterviewId() + "_full.mp3";
                fullPath = storageService.mergeFiles(request.getAllPaths(), subDir, fullFileName);
            }

            if (!request.getUserPaths().isEmpty()) {
                String userFileName = "interview_" + request.getInterviewId() + "_user.mp3";
                userPath = storageService.mergeFiles(request.getUserPaths(), subDir, userFileName);
            }

            return Map.of("status", "merged", "fullPath", fullPath, "userPath", userPath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to merge audio: " + e.getMessage(), e);
        }
    }

    public static class MergeRequest {
        private Long interviewId;
        private List<String> allPaths;
        private List<String> userPaths;

        public Long getInterviewId() {
            return interviewId;
        }

        public void setInterviewId(Long interviewId) {
            this.interviewId = interviewId;
        }

        public List<String> getAllPaths() {
            return allPaths;
        }

        public void setAllPaths(List<String> allPaths) {
            this.allPaths = allPaths;
        }

        public List<String> getUserPaths() {
            return userPaths;
        }

        public void setUserPaths(List<String> userPaths) {
            this.userPaths = userPaths;
        }
    }

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file, @RequestParam("interviewId") Long interviewId) {
        String filename = file.getOriginalFilename();

        try {
            Map<String, Object> payload = Map.of(
                    "interviewId", interviewId,
                    "filename", filename != null ? filename : "unknown",
                    "action", "ANALYZE_PROSODY"
            );
            String json = objectMapper.writeValueAsString(payload);
            redisTemplate.opsForList().leftPush(MEDIA_PROCESSING_QUEUE, json);
            logger.info("Queued media processing for interviewId={}, filename={}", interviewId, filename);
        } catch (JsonProcessingException e) {
            logger.error("Failed to queue media processing for interviewId={}", interviewId, e);
            return "Failed to queue for processing";
        }

        return "Upload Accepted. Processing in background.";
    }

    /**
     * Stream any stored audio file by its path with full HTTP range request support.
     * URL: GET /media/stream?path=<encoded_path>
     * Supports partial content requests for audio seeking and buffering.
     */
    @GetMapping("/stream")
    public ResponseEntity<?> streamAudio(
            @RequestParam String path,
            @RequestHeader(value = "Range", required = false) String rangeHeader) {
        try {
            // Load the audio data
            byte[] audioData = storageService.loadFile(path);
            if (audioData == null || audioData.length == 0) {
                return ResponseEntity.notFound().build();
            }

            // Determine content type based on extension
            String contentType = "audio/mpeg";
            if (path.endsWith(".webm")) {
                contentType = "audio/webm";
            } else if (path.endsWith(".ogg")) {
                contentType = "audio/ogg";
            } else if (path.endsWith(".wav")) {
                contentType = "audio/wav";
            } else if (path.endsWith(".mp3")) {
                contentType = "audio/mpeg";
            } else if (path.endsWith(".mp4")) {
                contentType = "audio/mp4";
            } else if (path.endsWith(".m4a")) {
                contentType = "audio/mp4";
            }

            String fileName = path.contains("/") ? path.substring(path.lastIndexOf("/") + 1) : path;

            // Handle range requests for seeking/buffering support
            if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
                return handleRangeRequest(audioData, contentType, fileName, rangeHeader);
            }

            // Full content response
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                    .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                    .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(audioData.length))
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(audioData);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to stream audio: " + e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body("Access denied");
        }
    }

    /**
     * Handle HTTP range requests for partial content delivery.
     * Essential for audio seeking and buffering in browsers.
     */
    private ResponseEntity<?> handleRangeRequest(byte[] data, String contentType, String fileName, String rangeHeader) {
        long fileSize = data.length;

        // Parse range header: "bytes=start-end"
        String range = rangeHeader.replace("bytes=", "");
        long start = 0;
        long end = fileSize - 1;

        try {
            if (range.startsWith("-")) {
                // Suffix range: "bytes=-500" means last 500 bytes
                long suffixLength = Long.parseLong(range.substring(1));
                start = Math.max(0, fileSize - suffixLength);
            } else if (range.endsWith("-")) {
                // Open-ended range: "bytes=500-"
                start = Long.parseLong(range.substring(0, range.length() - 1));
            } else {
                // Explicit range: "bytes=0-499"
                String[] parts = range.split("-");
                start = Long.parseLong(parts[0]);
                end = Long.parseLong(parts[1]);
            }
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body("Invalid range header");
        }

        // Validate range
        if (start < 0 || start >= fileSize || end < start || end >= fileSize) {
            return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE)
                    .header(HttpHeaders.CONTENT_RANGE, "bytes */" + fileSize)
                    .build();
        }

        // Extract the requested byte range
        int startInt = (int) start;
        int length = (int) (end - start + 1);
        byte[] partialContent = new byte[length];
        System.arraycopy(data, startInt, partialContent, 0, length);

        // Return partial content with 206 status
        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                .header(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + end + "/" + fileSize)
                .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(length))
                .contentType(MediaType.parseMediaType(contentType))
                .body(partialContent);
    }
}
