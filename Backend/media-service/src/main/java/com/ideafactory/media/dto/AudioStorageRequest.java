package com.ideafactory.media.dto;

import java.io.Serializable;

public class AudioStorageRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long messageId;
    private Long interviewId;
    private Long userId;
    private byte[] audioData;
    private String replyTo;

    public AudioStorageRequest() {
    }

    public AudioStorageRequest(Long messageId, Long interviewId, Long userId, byte[] audioData, String replyTo) {
        this.messageId = messageId;
        this.interviewId = interviewId;
        this.userId = userId;
        this.audioData = audioData;
        this.replyTo = replyTo;
    }

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public Long getInterviewId() {
        return interviewId;
    }

    public void setInterviewId(Long interviewId) {
        this.interviewId = interviewId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public byte[] getAudioData() {
        return audioData;
    }

    public void setAudioData(byte[] audioData) {
        this.audioData = audioData;
    }

    public String getReplyTo() {
        return replyTo;
    }

    public void setReplyTo(String replyTo) {
        this.replyTo = replyTo;
    }
}