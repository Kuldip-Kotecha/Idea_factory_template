package com.ideafactory.user.queue;

public interface QueueService {
    void publishUserDeleted(Long userId, String email);
    void publishEmailNotification(String to, String subject, String text, String ctaUrl);
}