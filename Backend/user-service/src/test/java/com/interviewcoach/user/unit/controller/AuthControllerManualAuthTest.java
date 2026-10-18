package com.interviewcoach.user.unit.controller;

import com.interviewcoach.user.controller.AuthController;
import com.interviewcoach.user.domain.User;
import com.interviewcoach.user.queue.QueueService;
import com.interviewcoach.user.repository.UserRepository;
import com.interviewcoach.user.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = AuthController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
        com.interviewcoach.user.config.SecurityConfig.class,
        com.interviewcoach.user.security.JwtAuthenticationFilter.class
}))
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
@TestPropertySource(properties = {
        "APP_FRONTEND_URL=http://localhost:4200",
        "MEDIA_SERVICE_URL=http://localhost:8083"
})
@SuppressWarnings("null")
class AuthControllerManualAuthTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private QueueService queueService;

    @MockBean
    private ClientRegistrationRepository clientRegistrationRepository;

    @MockBean
    private com.interviewcoach.user.security.OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    @Test
    void register_createsUnverifiedLocalUserAndQueuesVerificationEmail() throws Exception {
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Alice\",\"email\":\"Alice@Example.com\",\"password\":\"Password123!\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Verification email sent"));

        verify(userRepository).save(org.mockito.ArgumentMatchers.argThat(user -> {
            assertThat(user.getName()).isEqualTo("Alice");
            assertThat(user.getEmail()).isEqualTo("alice@example.com");
            assertThat(user.getPasswordHash()).isEqualTo("encoded-password");
            assertThat(user.getProvider()).isEqualTo("local");
            assertThat(user.isEmailVerified()).isFalse();
            assertThat(user.getVerificationToken()).isNotBlank();
            assertThat(user.getVerificationTokenExpiresAt()).isAfter(Instant.now());
            return true;
        }));
        verify(queueService).publishEmailNotification(
                eq("alice@example.com"),
                eq("Verify your account email"),
                anyString(),
                org.mockito.ArgumentMatchers.argThat(ctaUrl -> ctaUrl != null && ctaUrl.contains("/verify-email?token=")));
    }

    @Test
    void login_returnsJwtForVerifiedLocalUser() throws Exception {
        User user = new User();
        user.setEmail("alice@example.com");
        user.setPasswordHash("encoded-password");
        user.setEmailVerified(true);

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password123!", "encoded-password")).thenReturn(true);
        when(jwtUtil.generateToken("alice@example.com")).thenReturn("jwt-token");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"alice@example.com\",\"password\":\"Password123!\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    @Test
    void login_rejectsUnverifiedLocalUser() throws Exception {
        User user = new User();
        user.setEmail("alice@example.com");
        user.setPasswordHash("encoded-password");
        user.setEmailVerified(false);

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password123!", "encoded-password")).thenReturn(true);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"alice@example.com\",\"password\":\"Password123!\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Email not verified"));
    }

    @Test
    void verifyEmail_marksUserVerifiedAndClearsToken() throws Exception {
        User user = new User();
        user.setEmail("alice@example.com");
        user.setVerificationToken("verify-token");
        user.setVerificationTokenExpiresAt(Instant.now().plusSeconds(300));

        when(userRepository.findByVerificationToken("verify-token")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(post("/api/auth/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"token\":\"verify-token\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Email verified"));

        verify(userRepository).save(org.mockito.ArgumentMatchers.argThat(saved -> {
            assertThat(saved.isEmailVerified()).isTrue();
            assertThat(saved.getVerificationToken()).isNull();
            assertThat(saved.getVerificationTokenExpiresAt()).isNull();
            return true;
        }));
    }
}
