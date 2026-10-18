package com.ideafactory.user.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ideafactory.user.domain.User;
import com.ideafactory.user.queue.QueueService;
import com.ideafactory.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test for the manual email/password auth flow.
 *
 * Run with: mvn test -Dspring.profiles.active=integration-test -Dtest=ManualAuthIntegrationTest
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("integration-test")
@SuppressWarnings("null")
class ManualAuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private QueueService queueService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("Manual auth: register, verify email, and login returns JWT")
    void manualAuth_registerVerifyLogin_succeeds() throws Exception {
        String email = "manual-auth-" + UUID.randomUUID() + "@example.com";

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "name": "Manual Auth User",
                          "email": "%s",
                          "password": "Password123!"
                        }
                        """.formatted(email)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Verification email sent"));

        User registeredUser = userRepository.findByEmail(email).orElseThrow();
        assertThat(registeredUser.getPasswordHash()).isNotBlank();
        assertThat(registeredUser.isEmailVerified()).isFalse();
        assertThat(registeredUser.getVerificationToken()).isNotBlank();

        verify(queueService).publishEmailNotification(
                eq(email),
                eq("Verify your account email"),
                anyString(),
                org.mockito.ArgumentMatchers.argThat(ctaUrl -> ctaUrl != null && ctaUrl.contains("/verify-email?token=")));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "email": "%s",
                          "password": "Password123!"
                        }
                        """.formatted(email)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Email not verified"));

        mockMvc.perform(post("/api/auth/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "token": "%s"
                        }
                        """.formatted(registeredUser.getVerificationToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Email verified"));

        Optional<User> verifiedUser = userRepository.findByEmail(email);
        assertThat(verifiedUser).isPresent();
        assertThat(verifiedUser.get().isEmailVerified()).isTrue();
        assertThat(verifiedUser.get().getVerificationToken()).isNull();

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "email": "%s",
                          "password": "Password123!"
                        }
                        """.formatted(email)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode body = objectMapper.readTree(loginResponse);
        assertThat(body.path("token").asText()).isNotBlank();
    }
}