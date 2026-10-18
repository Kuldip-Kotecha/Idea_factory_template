package com.interviewcoach.utility.tts.health;

import com.interviewcoach.utility.tts.service.TtsService;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class TtsHealthIndicator implements HealthIndicator {

    private final TtsService ttsService;

    public TtsHealthIndicator(TtsService ttsService) {
        this.ttsService = ttsService;
    }

    @Override
    public Health health() {
        try {
            String[] voices = ttsService.getAvailableVoices();
            if (voices != null && voices.length > 0) {
                return Health.up().withDetail("provider", ttsService.getClass().getSimpleName()).withDetail("available_voices", voices.length).build();
            }
            return Health.down().withDetail("provider", ttsService.getClass().getSimpleName()).withDetail("error", "No voices available").build();
        } catch (Exception e) {
            return Health.down().withDetail("provider", ttsService.getClass().getSimpleName()).withDetail("error", e.getMessage()).build();
        }
    }
}