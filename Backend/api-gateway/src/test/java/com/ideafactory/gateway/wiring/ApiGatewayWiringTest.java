package com.ideafactory.gateway.wiring;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Wiring test — verifies Spring context loads and fallback routes are registered.
 * Does NOT call any external/third-party services.
 * Runs at build time with dummy config (application-smoke.yml).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("smoke")
class ApiGatewayWiringTest {

    private static final Logger logger = LoggerFactory.getLogger(ApiGatewayWiringTest.class);

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private RouteDefinitionLocator routeDefinitionLocator;

    @Value("${CORE_SERVICE_URL:http://localhost:8081}")
    private String expectedCoreServiceUrl;

    @Test
    @DisplayName("Wiring: /fallback/core-service returns fallback response")
    void coreServiceFallback_returns503() {
        webTestClient.get()
                .uri("/fallback/core-service")
                .exchange()
                .expectStatus().is5xxServerError();
        String body = webTestClient.get()
                .uri("/fallback/core-service")
                .exchange()
                .returnResult(String.class)
                .getResponseBody()
                .blockFirst();
        assertThat(body).contains("\"error\"");
        assertThat(body).contains("Core business service is temporarily unavailable");
        logger.info("Wiring: /fallback/core-service returns proper fallback response");
    }

    @Test
    @DisplayName("Wiring: /fallback/utility-service returns fallback response")
    void utilityServiceFallback_returns503() {
        webTestClient.get()
                .uri("/fallback/utility-service")
                .exchange()
                .expectStatus().is5xxServerError();
        String body = webTestClient.get()
                .uri("/fallback/utility-service")
                .exchange()
                .returnResult(String.class)
                .getResponseBody()
                .blockFirst();
        assertThat(body).contains("Utility service is temporarily unavailable");
        logger.info("Wiring: /fallback/utility-service returns proper fallback response");
    }

    @Test
    @DisplayName("Wiring: /fallback/user-service returns fallback response")
    void userServiceFallback_returns503() {
        webTestClient.get()
                .uri("/fallback/user-service")
                .exchange()
                .expectStatus().is5xxServerError();
        logger.info("Wiring: /fallback/user-service returns proper fallback response");
    }

    @Test
    @DisplayName("Wiring: /fallback/media-service returns fallback response")
    void mediaServiceFallback_returns503() {
        webTestClient.get()
                .uri("/fallback/media-service")
                .exchange()
                .expectStatus().is5xxServerError();
        logger.info("Wiring: /fallback/media-service returns proper fallback response");
    }

    @Test
    @DisplayName("Wiring: Controllers are properly loaded")
    void controllers_areLoaded() {
        assertThat(webTestClient).isNotNull();
        logger.info("Wiring: API Gateway controllers loaded successfully");
    }

    @Test
    @DisplayName("Wiring: core-service route is registered in RouteDefinitionLocator")
    void coreServiceRoute_isRegistered() {
        RouteDefinition route = routeDefinitionLocator.getRouteDefinitions()
                .filter(definition -> "core-service".equals(definition.getId()))
                .blockFirst();

        assertThat(route).isNotNull();
        assertThat(route.getUri().toString()).isEqualTo(expectedCoreServiceUrl);
    }
}
