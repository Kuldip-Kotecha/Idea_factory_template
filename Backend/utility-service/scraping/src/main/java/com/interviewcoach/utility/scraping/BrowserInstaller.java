package com.interviewcoach.utility.scraping;

import com.microsoft.playwright.Playwright;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BrowserInstaller {
    private static final Logger logger = LoggerFactory.getLogger(BrowserInstaller.class);

    public static void main(String[] args) {
        logger.info("BrowserInstaller: Starting Chromium installation...");
        try (Playwright playwright = Playwright.create()) {
            long start = System.currentTimeMillis();

            logger.info("BrowserInstaller: Chromium installed in {}ms", System.currentTimeMillis() - start);
        } catch (Exception e) {
            logger.error("BrowserInstaller: Failed to install Chromium", e);
            System.exit(1);
        }
    }
}