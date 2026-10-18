package com.ideafactory.gateway.wiring;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.context.ActiveProfiles;

/**
 * Wiring test — verifies Spring context loads and actuator routes are registered.
 * Does NOT call any external/third-party services.
 * Runs at build time with dummy config (application-smoke.yml).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("smoke")
class ActuatorWiringTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void envEndpointShouldBeForbiddenOrNotFound() {
        webTestClient.get().uri("/actuator/env")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void healthEndpointShouldBeAvailable() {
        webTestClient.get().uri("/actuator/health")
                .exchange()
                .expectStatus().isOk();
    }
}
