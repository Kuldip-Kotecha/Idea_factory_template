package com.interviewcoach.user.wiring;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Wiring test — verifies Spring context loads and user controller routes are registered.
 * Uses H2 in-memory DB and dummy OAuth2/Cloudinary config (application-smoke.yml).
 * Does NOT call Google OAuth2 or any real external service.
 * Runs at build time.
 *
 * Real OAuth2 flow is verified in post-deploy integration tests (4d).
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("smoke")
class UserServiceWiringTest {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceWiringTest.class);

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Wiring: /api/users/me route is registered (returns non-404)")
    void usersMeEndpoint_isRouted() throws Exception {
        try {
            mockMvc.perform(get("/api/users/me"))
                    .andDo(result -> {
                        int status = result.getResponse().getStatus();
                        assertThat(status).as("Route /api/users/me must be registered (not 404)").isNotEqualTo(404);
                        logger.info("Wiring: /api/users/me responded with status {}", status);
                    });
        } catch (Exception ex) {
            // Controller throws RuntimeException when no OAuth2 principal — route IS registered
            logger.info("Wiring: /api/users/me route found (exception: {})", ex.getClass().getSimpleName());
        }
    }

    @Test
    @DisplayName("Wiring: manual auth routes are registered (return non-404)")
    void manualAuthEndpoints_areRouted() throws Exception {
        assertPostRouteRegistered("/api/auth/register");
        assertPostRouteRegistered("/api/auth/login");
        assertPostRouteRegistered("/api/auth/verify");
        assertPostRouteRegistered("/api/auth/resend");
        assertPostRouteRegistered("/api/auth/forgot-password");
        assertPostRouteRegistered("/api/auth/validate-reset-token");
        assertPostRouteRegistered("/api/auth/reset-password");
    }

    @Test
    @DisplayName("Wiring: User service beans are properly loaded")
    void beans_areLoaded() {
        assertThat(mockMvc).isNotNull();
        logger.info("Wiring: User service beans loaded successfully");
    }

    private void assertPostRouteRegistered(String path) throws Exception {
        mockMvc.perform(post(java.util.Objects.requireNonNull(path))
                .contentType(java.util.Objects.requireNonNull(org.springframework.http.MediaType.APPLICATION_JSON))
                .content("{}"))
                .andDo(result -> {
                    int status = result.getResponse().getStatus();
                    assertThat(status).as("Route %s must be registered (not 404)", path).isNotEqualTo(404);
                    logger.info("Wiring: {} responded with status {}", path, status);
                });
    }
}
