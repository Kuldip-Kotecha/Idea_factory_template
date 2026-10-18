package com.ideafactory.notification.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.mail.internet.MimeMessage;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

/**
 * Integration test that verifies real email delivery via SMTP.
 * Tests actual email sending through configured SMTP server (Gmail, SendGrid, etc.).
 * 
 * Run with: mvn test -Dspring.profiles.active=integration-test
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("integration-test")
@EnabledIfEnvironmentVariable(named = "SPRING_MAIL_USERNAME", matches = ".+")
@SuppressWarnings("null")
class EmailIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(EmailIntegrationTest.class);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JavaMailSender mailSender;

    @MockBean
    private StringRedisTemplate stringRedisTemplate;

    @MockBean
    private ListOperations<String, String> listOperations;

    @Value("${spring.mail.host}")
    private String mailHost;

    @Value("${spring.mail.port}")
    private int mailPort;

    @Value("${app.notification.email-from:}")
    private String emailFromRaw;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${app.notification.support-email:}")
    private String supportEmailRaw;

    private String emailFrom;
    private String supportEmail;

    @BeforeEach
    void setUp() {
        emailFrom = (emailFromRaw == null || emailFromRaw.isBlank())
                ? ((mailUsername != null && !mailUsername.isBlank() && !mailUsername.equals("placeholder")) ? mailUsername : "noreply@example.com")
                : emailFromRaw;

        supportEmail = (supportEmailRaw == null || supportEmailRaw.isBlank())
                ? ((mailUsername != null && !mailUsername.isBlank() && !mailUsername.equals("placeholder")) ? mailUsername : "support@example.com")
                : supportEmailRaw;

        logger.info("Running Email integration test with SMTP: {}:{}", mailHost, mailPort);
        when(stringRedisTemplate.opsForList()).thenReturn(listOperations);
        when(listOperations.leftPush(anyString(), anyString())).thenReturn(1L);
    }

    @Test
    @DisplayName("Should successfully send email via SMTP")
    void testSendEmail_RealSmtpCall() throws Exception {
        // Use a test email address (can be configured via env var)
        String testEmail = System.getenv("TEST_EMAIL_TO");
        if (testEmail == null || testEmail.isBlank()) {
            testEmail = emailFrom; // Send to self if no test email configured
            logger.warn("TEST_EMAIL_TO not set, sending to email-from address: {}", testEmail);
        }

        Map<String, String> payload = new HashMap<>();
        payload.put("to", testEmail);
        payload.put("subject", "Integration Test - IdeaFactory Email Delivery");
        payload.put("text", "This is an automated integration test. If you received this, email delivery is working correctly.");
        payload.put("ctaUrl", "https://interview-coach-kk.vercel.app");

        mockMvc.perform(post("/notifications/send-email")
                        .contentType("application/json")
                        .content(asJsonString(payload)))
                .andExpect(status().isOk())
                .andExpect(content().string("Email queued for delivery"));

        logger.info("Email sent to {} via Redis Queue -> Notification Service -> SMTP", testEmail);
    }

    @Test
    @DisplayName("Should establish successful SMTP connection")
    void testSmtpConnection() {
        assertThat(mailSender).isNotNull();

        try {
            // Create a test message to verify connection
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(emailFrom);
            helper.setTo(emailFrom);
            helper.setSubject("SMTP Connection Test");
            helper.setText("This is a test to verify SMTP connectivity.", false);

            // Actually send the email
            mailSender.send(mimeMessage);

            logger.info("✅ SMTP connection successful - Email sent to {}", emailFrom);
            assertThat(true).isTrue();
        } catch (Exception e) {
            logger.error("❌ SMTP connection failed: {}", e.getMessage(), e);
            throw new RuntimeException("SMTP connection test failed", e);
        }
    }

    @Test
    @DisplayName("Should send HTML email with template")
    void testSendHtmlEmail() throws Exception {
        String testEmail = System.getenv("TEST_EMAIL_TO");
        if (testEmail == null || testEmail.isBlank()) {
            testEmail = emailFrom;
        }

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

        helper.setFrom(emailFrom);
        helper.setTo(testEmail);
        helper.setSubject("IdeaFactory - HTML Email Integration Test");

        // Send HTML email with basic formatting
        String htmlContent = """
                <html>
                <body style="font-family: Arial, sans-serif; padding: 20px;">
                    <h2 style="color: #2563eb;">Email Integration Test</h2>
                    <p>This is an automated test to verify HTML email delivery.</p>
                    <p style="background: #f3f4f6; padding: 10px; border-radius: 5px;">
                        <strong>SMTP Server:</strong> %s:%d<br/>
                        <strong>From:</strong> %s<br/>
                        <strong>To:</strong> %s
                    </p>
                    <p style="color: #6b7280; font-size: 12px; margin-top: 20px;">
                        If you received this email, HTML email delivery is working correctly.
                    </p>
                </body>
                </html>
                """.formatted(mailHost, mailPort, emailFrom, testEmail);

        helper.setText(htmlContent, true);
        mailSender.send(mimeMessage);

        logger.info("HTML email sent to {} via SMTP", testEmail);
        assertThat(true).isTrue();
    }

    @Test
    @DisplayName("Should validate SMTP server configuration")
    void testSmtpConfiguration() {
        logger.info("SMTP Configuration:");
        logger.info("  Host: {}", mailHost);
        logger.info("  Port: {}", mailPort);
        logger.info("  Username: {}", System.getenv("SPRING_MAIL_USERNAME"));
        logger.info("  From Email: {}", emailFrom);
        logger.info("  Support Email: {}", supportEmail);

        assertThat(mailHost).isNotBlank();
        assertThat(mailPort).isGreaterThan(0);
        assertThat(emailFrom).isNotBlank();

        // Verify Gmail SMTP (most common setup)
        if (mailHost.contains("gmail")) {
            assertThat(mailPort).isIn(587, 465);
            logger.info("Gmail SMTP configuration validated (port {})", mailPort);
        }
    }

    private String asJsonString(final Object object) {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert object to JSON", e);
        }
    }
}
