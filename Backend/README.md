# InterviewCoach - Spring Boot Backend

This directory hosts the server-side application for InterviewCoach. It provides the REST API, manages the AI integration (OpenRouter / NVIDIA NIM), handles database operations, and manages real-time session state.

## 🛠️ Tech Stack

- **Language**: Java 21
- **Framework**: Spring Boot 3.x
- **Database**: MySQL 8.0 (Database-per-Service pattern)
- **Caching/PubSub**: Redis, RabbitMQ
- **AI**: OpenRouter (default) + NVIDIA NIM (optional)
- **Storage**: Cloudinary (Cloud-based file storage and CDN)
- **Tracing**: Zipkin

## 🧩 Microservices

| Service                  | Port   | Responsibilities                                                          |
| :----------------------- | :----- | :------------------------------------------------------------------------ |
| **API Gateway**          | `8080` | Entry point, global CORS, Authorization header propagation.               |
| **Core Service**         | `8081` | Manages Interviews, Questions, Feedback, and History.                     |
| **Media Service**        | `8082` | Handles audio/video uploads and interacts with Cloudinary/Storage.        |
| **User Service**         | `8083` | Manages User identities, Google OAuth2 login flows.                       |
| **Notification Service** | `8084` | Sends transactional emails based on system events.                        |
| **Utility Service**      | `8085` | Specialized gateway for AI models (OpenRouter/NVIDIA NIM), TTS and STT.   |

## 📋 Prerequisites

- **Java JDK**: 21
- **Maven**: 3.8+
- **Docker**: For running the database, Redis, RabbitMQ, and Zipkin locally via [`Others/Database/`](../Others/Database).

## ⚙️ Configuration

The application requires environment variables for configuration. Create a `local.env` file in the root (use `local.env.example` as a template):

### Database (per service)

- `SPRING_DATASOURCE_URL_CORE` / `_MEDIA` / `_USER`
- `SPRING_DATASOURCE_USERNAME_CORE` / `_MEDIA` / `_USER`
- `SPRING_DATASOURCE_PASSWORD_CORE` / `_MEDIA` / `_USER`

### AI & Cloud

- `AI_PROVIDER`, `AI_MODEL`, `AI_API_KEY`, `AI_BASE_URL`
- `AI_TTS_PROVIDER`, `AI_TTS_API_KEY`, `AI_TTS_BASE_URL`
- `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`

### Infrastructure

- `SPRING_DATA_REDIS_HOST`, `SPRING_DATA_REDIS_PORT`
- `SPRING_RABBITMQ_HOST`, `SPRING_RABBITMQ_PORT`

### Storage

- `CLOUDINARY_CLOUD_NAME`, `CLOUDINARY_API_KEY`, `CLOUDINARY_API_SECRET`

## 🏗️ Build & Run

### 1. Start Infrastructure

Ensure the infrastructure is running first (from [`Others/Database/`](../Others/Database)):

```bash
cd ../Others/Database
./start.sh
# OR manually:
docker-compose -f docker-compose.db.yml up -d
docker-compose -f docker-compose.infra.yml up -d
```

### 2. Build the JAR

```bash
mvn clean package -DskipTests
```

### 3. Run with Docker Compose (Local Development)

```bash
docker-compose up -d --build
```

The API will be available at `http://localhost:8080`.

### 4. Run Locally (IntelliJ/Eclipse)

Ensure your IDE is configured with the environment variables listed above. Run the respective `*Application` main class for each service.

## 🚀 Production Deployment

Microservices are deployed to **Oracle Cloud VM** via Docker Compose using `docker-compose.prod.yml`.

Deployment scripts:
- `deploy-service.bat` — Build, push, and deploy a single service
- `deploy-oracle-cloud.bat` — Full Oracle Cloud deployment
- `setup-vm.sh` — Initial VM setup script

## 📖 API Documentation

Once the application is running, access Swagger UI at:
`http://localhost:8080/swagger-ui.html`
