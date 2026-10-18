package com.ideafactory.media.controller;

import com.ideafactory.media.service.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/media")
public class ArchiveController {

    private static final Logger logger = LoggerFactory.getLogger(ArchiveController.class);

    private final StorageService storageService;

    public ArchiveController(StorageService storageService) {
        this.storageService = storageService;
    }

    @PostMapping("/archive")
    public void archiveFolder(@RequestBody Map<String, String> payload) {
        String path = payload.get("path");
        if (path != null && !path.isEmpty()) {
            try {
                storageService.archiveFolder(path);
            } catch (IOException e) {
                // Log and continue
                logger.error("Failed to archive folder {}: {}", path, e.getMessage(), e);
            }
        }
    }
}
