package com.ideafactory.utility.ai.service;

import com.ideafactory.utility.ai.dto.GenerationRequest;

public interface GenAiService {
    String runSmokeCheck();
    String generate(GenerationRequest request);
    String transcribeAudio(byte[] audioData);
}