package com.interviewcoach.utility.tts.service;

public interface TtsService {
    byte[] generateAudio(String text, String voice);
    String transcribeAudio(byte[] audioData);
    String[] getAvailableVoices();
}