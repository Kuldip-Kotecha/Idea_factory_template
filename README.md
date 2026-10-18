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

## 🔐 Google Login Setup

The template includes an enterprise-ready, server-side OAuth 2.0 integration with Spring Security and Angular. Follow these 14 steps to configure Google Login for your deployment:

1. **Open the Google Cloud Console**: Navigate to [https://console.cloud.google.com/](https://console.cloud.google.com/) and sign in.
2. **Create or Select a Project**: Choose an existing project or click **New Project** to create one for your application.
3. **Configure OAuth Consent Screen**:
   - Go to **APIs & Services** > **OAuth consent screen**.
   - Choose user type **External** and click **Create**.
   - Fill in **App Name**, **User support email**, and **Developer contact email**.
4. **Configure Audience & Test Users**:
   - Add required scopes: `.../auth/userinfo.email`, `.../auth/userinfo.profile`, and `openid`.
   - While the app publishing status is **Testing**, add developer/tester Gmail accounts under **Test Users**.
5. **Create an OAuth 2.0 Client ID**:
   - Navigate to **APIs & Services** > **Credentials**.
   - Click **+ Create Credentials** > **OAuth client ID**.
6. **Select Application Type**: Choose **Web application**.
7. **Configure Authorized JavaScript Origins**:
   - `http://localhost:4200` (Angular Frontend)
   - `http://localhost:8080` (Spring Cloud API Gateway)
8. **Configure Authorized Redirect URIs**:
   - Primary (via Gateway): `http://localhost:8080/login/oauth2/code/google`
   - Standalone (direct to User Service): `http://localhost:8081/login/oauth2/code/google`
9. **Copy Generated Client ID**: Copy the Client ID string into your `.env` or environment configuration.
10. **Configure Client Secret**: Copy the Client Secret into backend environment variables (kept strictly on server side).
11. **Set Environment Variables**:
    Add credentials to `Other/Infrastructure/.env` (or system environment):
    ```bash
    GOOGLE_CLIENT_ID=your-client-id.apps.googleusercontent.com
    GOOGLE_CLIENT_SECRET=your-client-secret
    ```
12. **Start Frontend & Backend**:
    Launch `api-gateway` (port 8080), `user-service` (port 8081), and `Frontend` (port 4200).
13. **Test Google Login Locally**:
    - Open `http://localhost:4200/login`.
    - Click **Continue with Google**.
    - Complete authentication on Google; verify automatic redirect to `/dashboard` with an issued JWT token.
14. **Troubleshoot Common OAuth Errors**:
    - `redirect_uri_mismatch`: Check that the callback URI matches step 8 exactly.
    - `access_denied`: Verify tester email is added in step 4.
    - `invalid_client`: Verify `GOOGLE_CLIENT_SECRET` matches your Google Cloud console credentials.

### Configuration Reference

| Configuration | Purpose | Example / Default |
| :--- | :--- | :--- |
| `GOOGLE_CLIENT_ID` | Identifies the OAuth application to Google | `1234567890-xxx.apps.googleusercontent.com` |
| `GOOGLE_CLIENT_SECRET` | Used by server-side OAuth flow to exchange auth code for tokens | `GOCSPX-xxxxxxxxxxxxxxxxx` |
| Frontend URL | Single Page Application client origin | `http://localhost:4200` |
| Backend Gateway URL | Central API reverse proxy entry point | `http://localhost:8080` |
| User Service URL | Direct authentication microservice origin | `http://localhost:8081` |
| Authorized Redirect URI | Primary OAuth2 callback endpoint registered with Google | `http://localhost:8080/login/oauth2/code/google` |
| Fallback Redirect URI | Direct service callback if running without Gateway | `http://localhost:8081/login/oauth2/code/google` |

---

## 🏷️ Renaming the Base Package

The default package namespace across all microservices is `com.ideafactory`. When spinning up a new project:
1. Search and replace `com.ideafactory` with your target package (e.g., `com.mycompany.myproduct`) across all `.java` files.
2. Rename directory structures from `.../com/ideafactory` to match your package.
3. Update `<groupId>` in all `pom.xml` files.
4. For detailed step-by-step commands and scripts, see **Task 2** in [`Other/Documentation/ONBOARDING.md`](Other/Documentation/ONBOARDING.md#task-2-replace-base-package-name).

---

## 📖 Comprehensive Documentation

For complete setup instructions, environment variable configurations, database migrations, and architectural deep-dives, see:
👉 **[Developer Onboarding Guide (Other/Documentation/ONBOARDING.md)](Other/Documentation/ONBOARDING.md)**
👉 **[REST API Specifications (Other/Documentation/API_ENDPOINTS.md)](Other/Documentation/API_ENDPOINTS.md)**
👉 **[System Architecture Guide (Other/Architecture/ARCHITECTURE.md)](Other/Architecture/ARCHITECTURE.md)**

---

## 📄 License
MIT License. Built for rapid innovation.
