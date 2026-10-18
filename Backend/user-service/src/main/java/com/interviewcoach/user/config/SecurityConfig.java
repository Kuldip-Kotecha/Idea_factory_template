package com.interviewcoach.user.config;

import com.interviewcoach.user.security.JwtAuthenticationFilter;
import com.interviewcoach.user.security.OAuth2LoginSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class SecurityConfig {

        @Value("${APP_FRONTEND_URL:http://localhost:4200}")
        private String frontendUrl;

        private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
        private final JwtAuthenticationFilter jwtAuthenticationFilter;

        public SecurityConfig(OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler,
                        JwtAuthenticationFilter jwtAuthenticationFilter) {
                this.oAuth2LoginSuccessHandler = oAuth2LoginSuccessHandler;
                this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(csrf -> csrf.disable())
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/api/auth/**", "/login/**", "/oauth2/**", "/error",
                                                                "/actuator/**")
                                                .permitAll()
                                                .requestMatchers("/api/users/**").authenticated()
                                                .anyRequest().authenticated())
                                .oauth2Login(oauth2 -> oauth2
                                                .successHandler(oAuth2LoginSuccessHandler))
                                .logout(logout -> logout
                                                .logoutUrl("/api/auth/logout")
                                                .logoutSuccessUrl(frontendUrl + "/login?logout=true")
                                                .clearAuthentication(true))
                                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
                return http.build();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }
}
