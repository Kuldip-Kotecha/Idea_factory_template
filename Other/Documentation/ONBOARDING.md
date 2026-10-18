# Idea Factory Full-Stack Template — Developer Onboarding Guide

Welcome to the **Idea Factory Template**. This document is your comprehensive operational guide for spinning up, configuring, customizing, and deploying applications built on this standard template.

---

## Table of Contents
1. [Architecture Overview](#1-architecture-overview)
2. [Prerequisites & Local Environment Setup](#2-prerequisites--local-environment-setup)
3. [Configuration & Secrets Management](#3-configuration--secrets-management)
4. [Database Setup & Migrations](#4-database-setup--migrations)
5. [Running the Backend Services](#5-running-the-backend-services)
6. [Running the Frontend Application](#6-running-the-frontend-application)
7. [Full-Stack Local Development with Docker](#7-full-stack-local-development-with-docker)
8. [Authentication & Authorization Flow](#8-authentication--authorization-flow)
9. [Core Domain Architecture & Extensibility](#9-core-domain-architecture--extensibility)
10. [API Gateway & Resilience Patterns](#10-api-gateway--resilience-patterns)
11. [Notification System & Async Workflows](#11-notification-system--async-workflows)
12. [Testing Strategy & Test Suites](#12-testing-strategy--test-suites)
13. [Deployment & CI/CD Pipeline](#13-deployment--cicd-pipeline)
14. [Troubleshooting & FAQ](#14-troubleshooting--faq)
15. [Production Hardening Checklist](#15-production-hardening-checklist)

---

## 1. Architecture Overview

### High-Level Topology
The template adopts a decoupled microservices-based architecture fronted by a reactive API Gateway and an Angular 18 Single Page Application.

```
                         [ Browser / Client ]
                                  │
                                  ▼
                 [ API Gateway (Spring Cloud / Port 8080) ]
                   │            │             │          │
         ┌─────────┘            │             │          └─────────┐
         ▼                      ▼             ▼                    ▼
   [ User Service ]     [ Core Service ]  [ Utility / AI ]  [ Notification ]
     (Port 8081)          (Port 8082)       (Port 8084)       (Port 8083)
         │                      │                                  │
         ▼                      ▼                                  ▼
   [ app_users DB ]      [ app_core DB ]                    [ SMTP / Mail ]
```

### Service Boundaries & Responsibilities

| Service | Technology | Port | Responsibilities |
| :--- | :--- | :--- | :--- |
| **API Gateway** | Spring Cloud Gateway (Netty) | `8080` | Reverse proxy, Resilience4j circuit breakers, CORS, route protection |
| **User Service** | Spring Boot 3.2.3 (Tomcat) | `8081` | Authentication (JWT & Google OAuth2), user lifecycle, roles, RBAC |
| **Core Service** | Spring Boot 3.2.3 (Tomcat) | `8082` | Domain business features, REST CRUD APIs, starter feature module |
| **Notification Service** | Spring Boot 3.2.3 (Tomcat) | `8083` | Async email delivery (verification, password resets, system alerts) |
| **Utility Service** | Spring Boot 3.2.3 (Tomcat) | `8084` | AI completions (Hugging Face / OpenAI), Whisper audio STT |
| **Frontend** | Angular 18 (Standalone) | `4200` | Signal-based reactive state store, Tailwind CSS, admin & user views |

---

## 2. Prerequisites & Local Environment Setup

Ensure the following runtimes and SDKs are installed on your workstation:

| Tool | Recommended Version | Verification Command | Notes |
| :--- | :--- | :--- | :--- |
| **Java JDK** | 21 or 24 | `java -version` | OpenJDK or Temurin recommended |
| **Apache Maven** | 3.9+ | `mvn -version` | Used for backend builds and test suites |
| **Node.js** | 20+ (24 supported) | `node -v` | JavaScript runtime for Angular CLI |
| **npm** | 10+ | `npm -v` | Node package manager |
| **Docker & Compose** | 24+ | `docker --version` | Container runtime for local MySQL and services |
| **MySQL Client / Workbench** | 8.0+ | `mysql --version` | Database inspection and schema execution |

---

## 3. Configuration & Secrets Management

All environment variables and credentials are externalized. **Zero hardcoded credentials exist in tracked git files.**

### Environment Template
Copy the environment template from `Other/Infrastructure/.env.example` to `Other/Infrastructure/.env`:

```bash
cp Other/Infrastructure/.env.example Other/Infrastructure/.env
```

### Essential Variables
| Variable | Description | Default / Example |
| :--- | :--- | :--- |
| `MYSQL_HOST` | Database host | `localhost` or `mysql` |
| `MYSQL_PORT` | Database port | `3306` |
| `MYSQL_ROOT_PASSWORD` | MySQL root administrative password | Secure password |
| `USERS_DB_USERNAME` | User Service DB user | `auth_user` |
| `USERS_DB_PASSWORD` | User Service DB password | Secure password |
| `CORE_DB_USERNAME` | Core Service DB user | `core_user` |
| `CORE_DB_PASSWORD` | Core Service DB password | Secure password |
| `JWT_SECRET` | 256-bit secret key for HMAC-SHA256 tokens | Random 64-char hex string |
| `GOOGLE_CLIENT_ID` | OAuth2 Google Client ID | Google Cloud Console credential |
| `GOOGLE_CLIENT_SECRET` | OAuth2 Google Client Secret | Google Cloud Console credential |
| `SPRING_MAIL_USERNAME` | SMTP sender email address | `noreply@yourdomain.com` |
| `SPRING_MAIL_PASSWORD` | SMTP app-specific password | App password |
| `AI_API_KEY` | Hugging Face or inference provider key | `hf_...` |

---

## 4. Database Setup & Migrations

The template isolates microservice storage into individual database schemas with dedicated user permissions.

### Schemas
- `app_users`: User accounts, passwords, verification tokens, roles, and profiles.
- `app_core`: Business domain data (seeded with `sample_items`).
- `app_media`: Media uploads, attachments, and metadata.

### Quick Local Database Setup
Using the provided local Docker Compose script:
```bash
cd Other/Database/local-infra
# Windows:
.\start.bat
# Linux/macOS:
chmod +x start.sh && ./start.sh
```

This spins up MySQL 8.0 with health checks and executes:
1. `Other/Database/init.sql` (Creates databases and grants privileges)
2. `Other/Database/schemas/01_create_users_schema.sql` (Initializes users table)
3. `Other/Database/schemas/02_create_core_starter_schema.sql` (Initializes sample items table)

---

## 5. Running the Backend Services

You can run individual services during active feature development or orchestrate them together.

### Run All Backend Services via Maven
From the `Backend/` directory:
```bash
# Package all services
mvn clean package -DskipTests

# Run User Service
cd user-service && mvn spring-boot:run

# Run Core Service (in a second terminal)
cd core-service && mvn spring-boot:run

# Run API Gateway (in a third terminal)
cd api-gateway && mvn spring-boot:run

# Run Notification Service (in a fourth terminal)
cd notification-service && mvn spring-boot:run
```

---

## 6. Running the Frontend Application

The frontend is an Angular 18 single-page application utilizing zoneless-ready signal stores and Tailwind CSS.

### Development Server
From the `Frontend/` directory:
```bash
# Install dependencies
npm install

# Start development server with proxy to API Gateway (port 8080)
npm start
```
The application will be accessible at: `http://localhost:4200`

### Proxy Configuration
API calls to `/api/**`, `/oauth2/**`, and `/media/**` are automatically forwarded to `http://127.0.0.1:8080` via `Frontend/proxy.conf.json`.

---

## 7. Full-Stack Local Development with Docker

To boot the entire full-stack ecosystem (MySQL, API Gateway, all 4 microservices, and Frontend Nginx) with a single command:

```bash
cd Other/Infrastructure
docker compose -f docker-compose.infra.yml up -d
```

To view logs across all containers:
```bash
docker compose -f docker-compose.infra.yml logs -f
```

---

## 8. Authentication & Authorization Flow

### JWT Lifecycle
1. **Local Authentication**: User submits email + password to `POST /api/auth/login`. On credential validation, a signed JWT token containing email, user ID, and roles is returned.
2. **Client Storage**: The Angular application persists the token in `localStorage` under key `jwt_token`.
3. **HTTP Interception**: Angular's `authInterceptor` appends `Authorization: Bearer <token>` to all protected HTTP requests.
4. **Gateway & Downstream**: The API Gateway forwards the header to backend services; Spring Security filter (`JwtAuthenticationFilter`) validates the signature and populates the `SecurityContext`.

### Role-Based Access Control (RBAC)
- Standard User: `ROLE_USER`
- Administrator: `ROLE_ADMIN`
- Frontend Guard: `adminGuard` protects routes (e.g., `/users`) by checking user permissions.
- Backend Annotation: Use `@PreAuthorize("hasRole('ADMIN')")` on sensitive controller endpoints.

---

## 9. Core Domain Architecture & Extensibility

### Adding a New Business Feature
To implement a new business idea:
1. **Database Schema**: Add a migration script in `Other/Database/schemas/` (e.g., `03_create_orders_schema.sql`).
2. **Backend Domain Entity**: Create the JPA entity in `Backend/core-service/src/main/java/com/interviewcoach/core/entity/`.
3. **Repository**: Create a Spring Data JPA repository extending `JpaRepository`.
4. **Service**: Implement domain logic with transaction boundaries (`@Transactional`).
5. **Controller**: Expose REST endpoints under `/api/core/<resource>` with DTO validation (`@Valid`).
6. **Frontend Service**: Create a client service in `Frontend/src/app/services/` using Angular `HttpClient`.
7. **Frontend Component**: Add a standalone component and register the route in `Frontend/src/app/app.routes.ts`.

---

## 10. API Gateway & Resilience Patterns

The API Gateway (`api-gateway`) is built on Spring Cloud Gateway with Resilience4j circuit breakers:
- **Fallback Endpoints**: If `user-service`, `core-service`, or `utility-service` becomes unresponsive, circuit breakers open and return standard JSON fallbacks (`503 Service Unavailable`).
- **Health Verification**: Monitor gateway health at `http://localhost:8080/actuator/health`.

---

## 11. Notification System & Async Workflows

The `notification-service` listens for email delivery requests asynchronously:
- Email verification links
- Password reset tokens
- System alerts and welcome emails

For high-throughput requirements, connect the service to RabbitMQ or Apache Kafka by enabling the messaging dependencies in `Backend/notification-service/pom.xml`.

---

## 12. Testing Strategy & Test Suites

The template includes test coverage across all layers:
- **Unit Tests**: Mocked dependencies testing business logic.
- **Slice Tests**: `@WebMvcTest` verifying controller routes, serialization, and error responses.
- **Wiring Tests**: Smoke tests confirming Spring application contexts boot without circular references.

To run test suites across all backend microservices:
```bash
cd Backend
mvn test
```

---

## 13. Deployment & CI/CD Pipeline

Production deployment configurations are located in `Other/Deployment/`:
- `docker-compose.prod.yml`: Production multi-container layout with resource limits.
- `setup-vm.sh`: Host initialization script for Ubuntu 22.04 LTS (Docker, UFW firewall, fail2ban).
- `deploy-service.bat`: Automated build and deployment helper for local or remote staging.

---

## 14. Troubleshooting & FAQ

### Port Conflicts
- Port `8080` in use: Change gateway port in `Backend/api-gateway/src/main/resources/application.yml`.
- Port `3306` in use: Map host port to `3307` in `docker-compose.db.yml`.

### CORS Issues
- Ensure `API_GATEWAY_URL` or `frontendUrl` is properly configured in Spring Security.

---

## 15. Production Hardening Checklist

- [ ] Rotate all default passwords and generate high-entropy `JWT_SECRET`.
- [ ] Enable TLS/SSL certificates (Let's Encrypt / Certbot) on Nginx or Cloudflare reverse proxy.
- [ ] Configure database backups with retention policies.
- [ ] Enable rate-limiting filters on sensitive authentication endpoints.
- [ ] Configure Prometheus and Grafana for metrics collection from `/actuator/prometheus`.
