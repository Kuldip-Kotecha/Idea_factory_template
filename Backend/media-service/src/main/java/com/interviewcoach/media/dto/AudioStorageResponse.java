package com.interviewcoach.media.dto;

import java.io.Serializable;

public class AudioStorageResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long messageId;
    private String audioPath;
    private Double durationSeconds;
    private String error;

    public AudioStorageResponse() {
    }

    public AudioStorageResponse(Long messageId, String audioPath, Double durationSeconds) {
        this.messageId = messageId;
        this.audioPath = audioPath;
        this.durationSeconds = durationSeconds;
    }

    public AudioStorageResponse(Long messageId, String error) {
        this.messageId = messageId;
        this.error = error;
    }

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public String getAudioPath() {
        return audioPath;
    }

    public void setAudioPath(String audioPath) {
        this.audioPath = audioPath;
    }

    public Double getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Double durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}