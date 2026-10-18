package com.ideafactory.user.unit.controller;

import com.ideafactory.user.controller.UserController;
import com.ideafactory.user.domain.User;
import com.ideafactory.user.queue.QueueService;
import com.ideafactory.user.repository.UserRepository;
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
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = UserController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
        com.ideafactory.user.config.SecurityConfig.class,
        com.ideafactory.user.security.JwtAuthenticationFilter.class,
        com.ideafactory.user.security.JwtUtil.class
}))
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
@org.springframework.test.context.TestPropertySource(properties = {
        "MEDIA_SERVICE_URL=http://localhost:8083",
        "jwt.secret=test-secret-key-for-testing-purposes-only-12345678901234567890"
})
@SuppressWarnings("null")
public class UserControllerTest {

    @org.springframework.boot.test.mock.mockito.MockBean
    private com.ideafactory.user.security.OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    @org.springframework.boot.test.mock.mockito.MockBean
    private com.ideafactory.user.security.JwtUtil jwtUtil;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private com.ideafactory.user.service.StorageService storageService;

    @MockBean
    private QueueService queueService;

    @MockBean
    private ClientRegistrationRepository clientRegistrationRepository;

    @Test
    public void testGetCurrentUser_PreventsIDOR() throws Exception {
        String aliceEmail = "alice@example.com";
        User alice = new User();
        alice.setEmail(aliceEmail);
        alice.setName("Alice");
        alice.setPicture("alice.jpg");

        String bobEmail = "bob@example.com";
        User bob = new User();
        bob.setEmail(bobEmail);
        bob.setName("Bob");
        bob.setPicture("bob.jpg");

        when(userRepository.findByEmail(aliceEmail)).thenReturn(Optional.of(alice));
        when(userRepository.findByEmail(bobEmail)).thenReturn(Optional.of(bob));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

        mockMvc.perform(get("/api/users/me")
                .param("email", bobEmail)
                .with(oauth2Login().attributes(attrs -> attrs.put("email", aliceEmail))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(aliceEmail));
    }

    @Test
    public void testRegister_PreventsIDOR() throws Exception {
        String aliceEmail = "alice@example.com";
        User alice = new User();
        alice.setEmail(aliceEmail);
        alice.setName("Alice");
        alice.setPicture("alice.jpg");
        alice.setId(1L);

        String bobEmail = "bob@example.com";
        User bob = new User();
        bob.setEmail(bobEmail);
        bob.setName("Bob");
        bob.setPicture("bob.jpg");
        bob.setId(2L);

        when(userRepository.findByEmail(aliceEmail)).thenReturn(Optional.of(alice));
        when(userRepository.findByEmail(bobEmail)).thenReturn(Optional.of(bob));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

        String requestBody = "{\"email\": \"bob@example.com\", \"name\": \"Hacked Bob\", \"picture\": \"hacked.jpg\"}";

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .with(oauth2Login().attributes(attrs -> attrs.put("email", aliceEmail))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(aliceEmail));
    }
}