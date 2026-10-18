package com.interviewcoach.user.security;

import com.interviewcoach.user.domain.User;
import com.interviewcoach.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Value("${APP_FRONTEND_URL:http://localhost:4200}")
    private String frontendUrl;

    public OAuth2LoginSuccessHandler(JwtUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String picture = (String) oAuth2User.getAttribute("picture");

        // Upsert user in DB
        userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setName(name);
            newUser.setPicture(picture);
            newUser.setProvider("google");
            newUser.setEmailVerified(true);
            newUser.setRole(User.Role.USER);
            return userRepository.save(newUser);
        });

        // Generate JWT
        String token = jwtUtil.generateToken(email);

        // Redirect to Frontend with Token
        String targetUrl = frontendUrl + "/login?token=" + token;
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
