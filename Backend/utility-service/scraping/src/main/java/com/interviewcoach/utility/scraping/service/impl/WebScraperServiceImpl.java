package com.interviewcoach.utility.scraping.service.impl;

import com.interviewcoach.utility.scraping.service.WebScraperService;
import com.interviewcoach.utility.scraping.util.WebScraperUtility;
import org.springframework.stereotype.Service;

@Service
public class WebScraperServiceImpl implements WebScraperService {

    private final WebScraperUtility webScraperUtility;

    public WebScraperServiceImpl(WebScraperUtility webScraperUtility) {
        this.webScraperUtility = webScraperUtility;
    }

    @Override
    public String fetchPageContent(String url) {
        return webScraperUtility.fetchPageContent(url);
    }
}