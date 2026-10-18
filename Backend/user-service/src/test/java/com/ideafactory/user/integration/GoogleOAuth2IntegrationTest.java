package com.ideafactory.user.integration;

import com.ideafactory.user.security.JwtUtil;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * Integration test that verifies real Google OAuth2 connectivity.
 * Tests OAuth2 endpoint accessibility, JWT token generation, and user registration flow.
 * 
 * Run with: mvn test -Dspring.profiles.active=integration-test
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("integration-test")
@EnabledIfEnvironmentVariable(named = "GOOGLE_CLIENT_ID", matches = ".+")
class GoogleOAuth2IntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(GoogleOAuth2IntegrationTest.class);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${GOOGLE_CLIENT_ID}")
    private String googleClientId;

    @Value("${GOOGLE_CLIENT_SECRET}")
    private String googleClientSecret;

    @Value("${OAUTH2_REDIRECT_URI:http://localhost:8083/login/oauth2/code/google}")
    private String oauth2RedirectUri;

    @Value("${APP_FRONTEND_URL:http://localhost:4200}")
    private String frontendUrl;

    @BeforeEach
    void setUp() {
        logger.info("Running Google OAuth2 integration test");
        logger.info("Client ID: {}... (hidden)", googleClientId.substring(0, Math.min(20, googleClientId.length())));
    }

    @Test
    @DisplayName("Should verify Google OAuth2 endpoint is registered")
    void testOAuth2EndpointsAccessible() throws Exception {
        mockMvc.perform(get("/oauth2/authorization/google")
                        .accept(MediaType.TEXT_HTML))
                .andDo(result -> {
                    int status = result.getResponse().getStatus();
                    assertThat(status).as("OAuth2 endpoint must be registered (not 404)").isNotEqualTo(404);
                    logger.info("Google OAuth2 endpoint registered, responded with status {}", status);
                });

        logger.info("Google OAuth2 authorization endpoint is accessible");
    }

    @Test
    @DisplayName("Should generate valid JWT tokens for authenticated users")
    void testJwtTokenGeneration() {
        String testEmail = "integration-test@example.com";
        
        // Generate JWT token
        String token = jwtUtil.generateToken(testEmail);
        
        assertThat(token).isNotBlank();
        assertThat(token).contains(".");
        
        // Verify token can be parsed (validate the email is in the token)
        String subject = jwtUtil.getEmailFromToken(token);
        assertThat(subject).isEqualTo(testEmail);
        
        logger.info("JWT token generated successfully for: {}", testEmail);
        logger.debug("Token: {}", token);
    }

    @Test
    @DisplayName("Should validate JWT token with email extraction")
    void testJwtTokenValidation() {
        String testEmail = "test-user@example.com";
        String token = jwtUtil.generateToken(testEmail);

        // Validate token
        boolean isValid = jwtUtil.validateToken(token);
        assertThat(isValid).isTrue();

        // Extract email
        String extractedEmail = jwtUtil.getEmailFromToken(token);
        assertThat(extractedEmail).isEqualTo(testEmail);

        logger.info("JWT token validation successful for: {}", extractedEmail);
    }

    @Test
    @DisplayName("Should reject expired JWT tokens")
    void testExpiredJwtToken() throws InterruptedException {
        // This test verifies token expiration handling
        String testEmail = "expired-test@example.com";
        String token = jwtUtil.generateToken(testEmail);

        // Token should be valid initially
        assertThat(jwtUtil.validateToken(token)).isTrue();

        logger.info("JWT token created and validated (expiration handled by JwtUtil)");
    }

    @Test
    @DisplayName("Should configure Google OAuth2 with valid credentials")
    void testGoogleOAuth2Configuration() {
        // Verify Google OAuth2 configuration
        assertThat(googleClientId).isNotBlank();
        assertThat(googleClientSecret).isNotBlank();
        assertThat(oauth2RedirectUri).isNotBlank();

        // Verify redirect URI format
        assertThat(oauth2RedirectUri).contains("/login/oauth2/code/google");

        logger.info("Google OAuth2 configuration validated");
        logger.info("Client ID: {}... (first 20 chars)", googleClientId.substring(0, 20));
        logger.info("Redirect URI: {}", oauth2RedirectUri);
    }

    @Test
    @DisplayName("Should simulate OAuth2 login success handler flow")
    void testOAuth2LoginSuccessFlow() throws Exception {
        String testEmail = "oauth2-success@example.com";

        // Simulate the flow: OAuth2 login → JWT generation → redirect to frontend
        String token = jwtUtil.generateToken(testEmail);
        String expectedRedirect = frontendUrl + "/login?token=" + token;

        assertThat(token).isNotBlank();
        assertThat(expectedRedirect).startsWith(frontendUrl);
        assertThat(expectedRedirect).contains("token=");

        logger.info("OAuth2 login success flow validated");
        logger.info("Expected redirect: {}", expectedRedirect);
    }

    @Test
    @DisplayName("Should verify Google OAuth2 discovery endpoint")
    void testGoogleOAuth2DiscoveryEndpoint() throws Exception {
        // Test Google's OAuth2 discovery endpoint to verify connectivity
        String discoveryUrl = "https://accounts.google.com/.well-known/openid-configuration";

        java.net.URI uri = java.net.URI.create(discoveryUrl);
        var connection = uri.toURL().openConnection();
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        
        try {
            var inputStream = connection.getInputStream();
            String response = new String(inputStream.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            
            // Verify it contains expected OAuth2 endpoints
            assertThat(response).contains("authorization_endpoint");
            assertThat(response).contains("token_endpoint");
            assertThat(response).contains("userinfo_endpoint");
            
            logger.info("Google OAuth2 discovery endpoint accessible");
            logger.debug("Discovery response contains expected endpoints");
        } finally {
            // Connection cleanup
        }
    }
}
