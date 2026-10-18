package com.interviewcoach.utility.scraping.util;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitUntilState;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.net.InetAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.List;

@Component
public class WebScraperUtility {

    private static final Logger logger = LoggerFactory.getLogger(WebScraperUtility.class);
    private final ThreadLocal<Document> cachedDocument = new ThreadLocal<>();

    private final ExecutorService playwrightExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "playwright-worker");
        t.setDaemon(true);
        return t;
    });

    private Playwright playwright;
    private Browser browser;

    @PostConstruct
    public void init() {
        logger.info("WebScraperUtility: Initializing Playwright & Chromium browser on dedicated worker thread...");
        playwrightExecutor.submit(() -> {
            try {
                playwright = Playwright.create();
                browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                        .setHeadless(true)
                        .setArgs(List.of("--no-sandbox", "--disable-setuid-sandbox", "--disable-dev-shm-usage", "--disable-gpu")));
                logger.info("WebScraperUtility: Playwright and Chromium browser initialized successfully on worker thread");
            } catch (Exception e) {
                logger.error("WebScraperUtility: Failed to initialize Playwright on worker thread", e);
            }
        });
    }

    @PreDestroy
    public void destroy() {
        logger.info("WebScraperUtility: Closing Playwright and Chromium browser...");
        try {
            playwrightExecutor.submit(() -> {
                try {
                    if (browser != null) {
                        browser.close();
                    }
                    if (playwright != null) {
                        playwright.close();
                    }
                    logger.info("WebScraperUtility: Playwright and Browser closed successfully");
                } catch (Exception e) {
                    logger.error("WebScraperUtility: Error closing Playwright/Browser during destroy", e);
                }
            }).get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.warn("WebScraperUtility: Timeout or interruption during shutdown", e);
        }
        playwrightExecutor.shutdown();
        try {
            if (!playwrightExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                playwrightExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            playwrightExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    void validateUrl(String url) {
        if (url == null || url.isEmpty()) {
            throw new IllegalArgumentException("URL cannot be empty");
        }
        try {
            URI uri = new URI(url);
            String scheme = uri.getScheme();
            if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
                throw new RuntimeException("Invalid URL scheme. Only HTTP/HTTPS are allowed.");
            }
            String host = uri.getHost();
            if (host == null) {
                throw new RuntimeException("Invalid URL: Host is missing.");
            }
            InetAddress inetAddress = InetAddress.getByName(host);
            if (inetAddress.isLoopbackAddress() || inetAddress.isSiteLocalAddress() || inetAddress.isLinkLocalAddress() || inetAddress.isAnyLocalAddress()) {
                throw new RuntimeException("Blocked: Internal network access is denied.");
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException("Invalid URL format: " + e.getMessage());
        } catch (UnknownHostException e) {
            throw new RuntimeException("Could not resolve host: " + e.getMessage());
        }
    }

    public String fetchPageContent(String url) {
        long startTime = System.currentTimeMillis();
        logger.info("WebScraperUtility: Fetching content for URL: {}", url);
        validateUrl(url);
        String structuredStatus = "not-run";
        String jsoupStatus = "not-run";
        String playwrightStatus = "not-run";
        try {
            String text = tryStructuredData(url);
            if (!isInsufficient(text)) {
                logger.info("WebScraperUtility: Resolved with Structured Data (JSON) in {}ms. Length: {}", (System.currentTimeMillis() - startTime), text.length());
                return text;
            }
            structuredStatus = describeStageResult(text);

            text = tryJsoup(url);
            if (!isInsufficient(text)) {
                logger.info("WebScraperUtility: Resolved with Jsoup DOM in {}ms. Length: {}", (System.currentTimeMillis() - startTime), text.length());
                return text;
            }
            jsoupStatus = describeStageResult(text);

            text = tryPlaywright(url);
            if (!isInsufficient(text)) {
                long duration = System.currentTimeMillis() - startTime;
                logger.info("WebScraperUtility: Resolved with Playwright in {}ms. Length: {}", duration, text.length());
                logger.info("WebScraperUtility: Playwright returned content preview (first 1000 chars): {}", text.substring(0, Math.min(1000, text.length())));
                return text;
            }
            playwrightStatus = describeStageResult(text);

            throw new RuntimeException("Failed to scrape URL: no usable content found [structured-data=" + structuredStatus + ", jsoup=" + jsoupStatus + ", playwright=" + playwrightStatus + "]");
        } finally {
            cachedDocument.remove();
        }
    }

    String tryJsoup(String url) {
        try {
            Document doc = cachedDocument.get();
            if (doc == null) {
                doc = fetchDocument(url);
                cachedDocument.set(doc);
            }
            return extractBodyText(doc);
        } catch (Exception e) {
            logger.debug("WebScraperUtility: Jsoup scrape failed for {}", url, e);
            return "";
        }
    }

    String tryStructuredData(String url) {
        try {
            Document doc = cachedDocument.get();
            if (doc == null) {
                doc = fetchDocument(url);
                cachedDocument.set(doc);
            }
            return extractNextData(doc);
        } catch (Exception e) {
            logger.debug("WebScraperUtility: Structured data scrape failed for {}", url, e);
            return "";
        }
    }

    String tryPlaywright(String url) {
        try {
            Future<String> future = playwrightExecutor.submit(() -> {
                if (browser == null || !browser.isConnected()) {
                    logger.warn("WebScraperUtility: Browser not initialized or disconnected. Launching dynamic instance...");
                    try {
                        if (browser != null) browser.close();
                        if (playwright != null) playwright.close();
                    } catch (Exception ignored) {}
                    playwright = Playwright.create();
                    browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                            .setHeadless(true)
                            .setArgs(chromiumLaunchArgs()));
                }
                try (BrowserContext context = browser.newContext(new Browser.NewContextOptions().setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"));
                     Page page = context.newPage()) {
                    page.navigate(url, new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED).setTimeout(15000));
                    waitForDynamicContent(page);
                    return extractBodyText(Jsoup.parse(page.content()));
                }
            });
            return future.get(20, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.debug("WebScraperUtility: Playwright scrape failed for {}", url, e);
            return "";
        }
    }

    List<String> chromiumLaunchArgs() {
        return List.of("--no-sandbox", "--disable-setuid-sandbox", "--disable-dev-shm-usage", "--disable-gpu");
    }

    String extractBodyText(Document doc) {
        if (doc == null || doc.body() == null) return "";
        doc.select("nav, footer, header, script, style, noscript, iframe, .cookie-banner, .cookie-notice, .modal, .popup").remove();
        Element focusedContent = doc.selectFirst("main, article, [role=main], #job-description, .job-description, .jobDescription, [class*=job-description], [class*=JobDescription]");
        String focusedText = focusedContent != null ? focusedContent.text() : "";
        if (!isInsufficient(focusedText)) return focusedText;
        return doc.body().text();
    }

    String extractNextData(Document doc) {
        if (doc == null) return "";
        org.jsoup.select.Elements ldJsons = doc.select("script[type=application/ld+json]");
        StringBuilder sb = new StringBuilder();
        for (Element el : ldJsons) {
            String json = el.html();
            if (json.contains("JobPosting") || json.contains("description") || json.contains("title")) {
                sb.append(json).append("\n");
            }
        }
        if (sb.length() > 0) return sb.toString();
        Element nextData = doc.getElementById("__NEXT_DATA__");
        if (nextData != null) {
            String nextPayload = nextData.html();
            if (nextPayload != null && !nextPayload.trim().isEmpty()) return nextPayload;
        }
        return "";
    }

    boolean isInsufficient(String text) {
        return text == null || text.trim().length() < 160;
    }

    private String describeStageResult(String text) {
        if (text == null) return "null";
        String trimmed = text.trim();
        if (trimmed.isEmpty()) return "empty";
        if (trimmed.length() < 160) return "insufficient-length:" + trimmed.length();
        return "usable-length:" + trimmed.length();
    }

    private Document fetchDocument(String url) throws java.io.IOException {
        return Jsoup.connect(url)
            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
            .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
            .header("Accept-Language", "en-US,en;q=0.5")
            .header("Accept-Encoding", "gzip, deflate")
            .header("Connection", "keep-alive")
            .header("Upgrade-Insecure-Requests", "1")
            .header("Sec-Fetch-Dest", "document")
            .header("Sec-Fetch-Mode", "navigate")
            .header("Sec-Fetch-Site", "none")
            .header("Cache-Control", "max-age=0")
            .timeout(6000)
            .ignoreHttpErrors(true)
            .followRedirects(true)
            .maxBodySize(0)
            .get();
    }

    private void waitForDynamicContent(Page page) {
        try {
            page.waitForSelector("script[type='application/ld+json'], #__NEXT_DATA__, main, article, [role='main'], #job-description, .job-description, .jobDescription, [class*=job-description], [class*=JobDescription]", new Page.WaitForSelectorOptions().setTimeout(5000));
        } catch (Exception ignored) {}
        try {
            page.waitForLoadState(LoadState.NETWORKIDLE, new Page.WaitForLoadStateOptions().setTimeout(5000));
        } catch (Exception ignored) {}
    }
}