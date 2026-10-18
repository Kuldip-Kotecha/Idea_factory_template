package com.interviewcoach.utility.tts.dto;

import lombok.Data;

@Data
public class SpeakRequest {
    private String text;
    private String tone;
}