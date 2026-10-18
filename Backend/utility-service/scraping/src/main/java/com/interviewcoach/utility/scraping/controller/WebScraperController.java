package com.interviewcoach.utility.scraping.controller;

import com.interviewcoach.utility.scraping.service.WebScraperService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/scraping")
public class WebScraperController {

    private static final Logger logger = LoggerFactory.getLogger(WebScraperController.class);

    private final WebScraperService webScraperService;

    public WebScraperController(WebScraperService webScraperService) {
        this.webScraperService = webScraperService;
    }

    @PostMapping("/scrape")
    public ResponseEntity<String> scrape(@RequestBody ScrapeRequest request) {
        logger.info("WebScraperController: Received scrape request for URL: {}", request.getUrl());
        try {
            String content = webScraperService.fetchPageContent(request.getUrl());
            return ResponseEntity.ok(content);
        } catch (Exception e) {
            logger.error("WebScraperController: Failed to scrape URL: {}", request.getUrl(), e);
            return ResponseEntity.internalServerError().body("Failed to scrape URL: " + e.getMessage());
        }
    }

    public static class ScrapeRequest {
        private String url;
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
    }
}