package com.interviewcoach.user.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(
            @org.springframework.lang.NonNull HttpServletRequest request,
            @org.springframework.lang.NonNull HttpServletResponse response,
            @org.springframework.lang.NonNull FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        String email = null;
        String jwt = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7);
            try {
                if (jwtUtil.validateToken(jwt)) {
                    email = jwtUtil.getEmailFromToken(jwt);
                }
            } catch (Exception e) {
                logger.error("Could not validate JWT token");
            }
        }

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // Create a mock OAuth2User so @AuthenticationPrincipal OAuth2User works in
            // controllers
            java.util.Map<String, Object> attributes = new java.util.HashMap<>();
            attributes.put("email", email);
            org.springframework.security.oauth2.core.user.OAuth2User oauth2User = new org.springframework.security.oauth2.core.user.DefaultOAuth2User(
                    // DefaultOAuth2User requires at least one authority when creating it this way
                    java.util.Collections.singleton(
                            new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER")),
                    attributes,
                    "email");

            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    oauth2User, null, oauth2User.getAuthorities());
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }

        filterChain.doFilter(request, response);
    }
}
