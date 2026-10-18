# Idea Factory — Full-Stack Application Template

A production-grade, reusable full-stack application template designed to rapidly launch new ideas without rebuilding foundational services.

---

## 🚀 Key Capabilities

- **Microservices Architecture**: Decoupled backend powered by Spring Boot 3.2 (User Service, Core Domain Service, Notification Service, AI Utility Service).
- **Resilient API Gateway**: Spring Cloud Gateway with Resilience4j circuit breakers, automated fallbacks, and centralized routing.
- **Enterprise Authentication & RBAC**: JWT token management, Google OAuth2, password reset, account verification, and role-based permissions (`ROLE_USER`, `ROLE_ADMIN`).
- **Reactive Frontend**: Angular 18 standalone components, signal-based state store (`AppStore`), Tailwind CSS, and user management screens.
- **Database Isolation**: Multi-schema MySQL 8.0 setup with dedicated service users and automated migration scripts.
- **Production DevOps**: Containerized with Docker & Docker Compose, production VM provisioning scripts, and zero hardcoded secrets.

---

## 📁 Repository Structure

```text
Idea_factory_template/
│
├── Frontend/                   # Angular 18 Single Page Application
│   ├── src/app/pages/          # Dashboard, Users (Admin), Profile, Auth, Landing
│   ├── src/app/services/       # Signal Store, AuthService, UserService, SampleItemService
│   └── src/app/guards/         # Route protection (authGuard, adminGuard)
│
├── Backend/                    # Spring Boot 3.2 Microservices
│   ├── api-gateway/            # Unified routing & Resilience4j circuit breakers (Port 8080)
│   ├── user-service/           # JWT & OAuth2 auth, user management, RBAC (Port 8081)
│   ├── core-service/           # Business domain REST APIs & starter module (Port 8082)
│   ├── notification-service/   # Async email notifications (Port 8083)
│   └── utility-service/        # Generic AI completions & Whisper STT (Port 8084)
│
├── Other/                      # Non-application assets & operations
│   ├── Database/               # Schemas, init scripts, and local-infra docker compose
│   ├── Infrastructure/         # Docker Compose profiles & .env.example template
│   ├── Deployment/             # Production deployment scripts & VM setup
│   ├── Architecture/           # Architectural diagrams (SVG, PNG) & ARCHITECTURE.md
│   └── Documentation/          # ONBOARDING.md, API_ENDPOINTS.md
│
└── README.md
```

---

## ⚡ Quick Start

### 1. Start Local Database
```bash
cd Other/Database/local-infra
# Windows:
.\start.bat
# Linux/macOS:
./start.sh
```

### 2. Run Backend Microservices
```bash
cd Backend
mvn clean package -DskipTests

# Start API Gateway (Port 8080)
cd api-gateway && mvn spring-boot:run

# In separate terminals:
cd ../user-service && mvn spring-boot:run
cd ../core-service && mvn spring-boot:run
cd ../notification-service && mvn spring-boot:run
```

### 3. Run Frontend
```bash
cd Frontend
npm install
npm start
```
Open [http://localhost:4200](http://localhost:4200) in your browser.

---

## 📖 Comprehensive Documentation

For complete setup instructions, environment variable configurations, database migrations, and architectural deep-dives, see:
👉 **[Developer Onboarding Guide (Other/Documentation/ONBOARDING.md)](Other/Documentation/ONBOARDING.md)**
👉 **[REST API Specifications (Other/Documentation/API_ENDPOINTS.md)](Other/Documentation/API_ENDPOINTS.md)**
👉 **[System Architecture Guide (Other/Architecture/ARCHITECTURE.md)](Other/Architecture/ARCHITECTURE.md)**

---

## 📄 License
MIT License. Built for rapid innovation.
