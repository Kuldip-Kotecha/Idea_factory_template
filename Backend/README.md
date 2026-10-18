# Idea Factory — Spring Boot Microservices Backend

This directory hosts the server-side microservice architecture for Idea Factory. It provides the REST APIs, manages user identity and authentication (JWT & OAuth2), handles database persistence across isolated schemas, provides async notification capabilities, and provides generic AI utilities.

## Services Architecture

- **api-gateway** (Port 8080): Spring Cloud Gateway with Resilience4j circuit breakers.
- **user-service** (Port 8081): Authentication, registration, password reset, and user administration.
- **core-service** (Port 8082): Business domain logic and starter entity modules (`sample_items`).
- **notification-service** (Port 8083): Async email delivery and system alerts.
- **utility-service** (Port 8084): Generic AI completion and Whisper audio transcription.

## Build and Run

```bash
# Package all services
mvn clean package -DskipTests

# Run tests across all services
mvn test
```
