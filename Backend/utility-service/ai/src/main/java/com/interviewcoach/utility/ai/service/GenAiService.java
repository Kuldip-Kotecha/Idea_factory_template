package com.interviewcoach.utility.ai.service;

import com.interviewcoach.utility.ai.dto.GenerationRequest;

public interface GenAiService {
    String runSmokeCheck();
    String generate(GenerationRequest request);
    String transcribeAudio(byte[] audioData);
}