package com.interviewcoach.user.unit.controller;

import com.interviewcoach.user.controller.UserController;
import com.interviewcoach.user.domain.User;
import com.interviewcoach.user.queue.QueueService;
import com.interviewcoach.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = UserController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
        com.interviewcoach.user.config.SecurityConfig.class,
        com.interviewcoach.user.security.JwtAuthenticationFilter.class,
        com.interviewcoach.user.security.JwtUtil.class
}))
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
@org.springframework.test.context.TestPropertySource(properties = {
        "MEDIA_SERVICE_URL=http://localhost:8083",
        "jwt.secret=test-secret-key-for-testing-purposes-only-12345678901234567890"
})
@SuppressWarnings("null")
public class UserControllerSecurityTest {

    @org.springframework.boot.test.mock.mockito.MockBean
    private com.interviewcoach.user.security.OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    @org.springframework.boot.test.mock.mockito.MockBean
    private com.interviewcoach.user.security.JwtUtil jwtUtil;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private com.interviewcoach.user.service.StorageService storageService;

    @MockBean
    private QueueService queueService;

    @MockBean
    private ClientRegistrationRepository clientRegistrationRepository;

    @Test
    public void testRegister_PreventsAccountTakeover_ViaIdInjection() throws Exception {
        String attackerEmail = "attacker@example.com";

        long victimId = 123L;

        when(userRepository.findByEmail(attackerEmail)).thenReturn(Optional.empty());

        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            User user = (User) i.getArguments()[0];
            if (user.getPicture() == null) {
                user.setPicture("default.jpg");
            }
            return user;
        });

        String requestBody = "{\"id\": " + victimId
                + ", \"email\": \"ignored@example.com\", \"name\": \"Attacker\", \"picture\": \"attacker.jpg\"}";

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .with(oauth2Login().attributes(attrs -> attrs.put("email", attackerEmail))))
                .andExpect(status().isOk());

        verify(userRepository).save(org.mockito.ArgumentMatchers.argThat(user -> {
            if (user.getId() != null && user.getId() == victimId) {
                throw new AssertionError("SECURITY VULNERABILITY: UserController accepted injected ID " + victimId);
            }
            return true;
        }));
    }
}