package com.interviewcoach.notification.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.internet.MimeMessage;

@Service
@SuppressWarnings("null")
public class NotificationProcessor {

    private static final Logger logger = LoggerFactory.getLogger(NotificationProcessor.class);

    private final JavaMailSender emailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.notification.email-from:}")
    private String emailFromRaw;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${app.notification.support-email:}")
    private String supportEmailRaw;

    public NotificationProcessor(JavaMailSender emailSender, TemplateEngine templateEngine) {
        this.emailSender = emailSender;
        this.templateEngine = templateEngine;
    }

    public void sendHtmlMessage(String to, String subject, String text, String ctaUrl) {
        try {
            MimeMessage mimeMessage = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            String from = (emailFromRaw == null || emailFromRaw.isBlank())
                    ? ((mailUsername != null && !mailUsername.isBlank() && !mailUsername.equals("placeholder")) ? mailUsername : "noreply@example.com")
                    : emailFromRaw;

            String support = (supportEmailRaw == null || supportEmailRaw.isBlank())
                    ? ((mailUsername != null && !mailUsername.isBlank() && !mailUsername.equals("placeholder")) ? mailUsername : "support@example.com")
                    : supportEmailRaw;

            logger.info("Sending email from: {} to: {}, using support email: {}", from, to, support);

            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);

            Context ctx = new Context();
            ctx.setVariable("subject", subject);
            ctx.setVariable("message", text);
            ctx.setVariable("ctaUrl", ctaUrl);
            ctx.setVariable("supportEmail", support);

            String htmlContent = templateEngine.process("email-template", ctx);
            helper.setText(htmlContent, true);

            emailSender.send(mimeMessage);
            logger.info("HTML email sent to: {}", to);
        } catch (Exception e) {
            logger.error("Failed to send email to {}: {}", to, e.getMessage(), e);
        }
    }
}
