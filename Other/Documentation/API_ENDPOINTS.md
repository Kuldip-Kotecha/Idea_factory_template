# REST API Endpoints Reference

All requests from the Frontend are routed through the **API Gateway** on port `8080`.

---

## 1. Authentication (`/api/auth/**`)
Routed to `user-service` (Port 8083).

| Method | Endpoint | Description | Auth Required | Request Body |
| :--- | :--- | :--- | :--- | :--- |
| `POST` | `/api/auth/register` | Register new user with email & password | No | `{ "name": "...", "email": "...", "password": "..." }` |
| `POST` | `/api/auth/verify` | Verify email with token | No | `{ "token": "..." }` |
| `POST` | `/api/auth/resend` | Resend verification email | No | `{ "email": "..." }` |
| `POST` | `/api/auth/login` | Log in and receive JWT token | No | `{ "email": "...", "password": "..." }` |
| `POST` | `/api/auth/forgot-password` | Request password reset email | No | `{ "email": "..." }` |
| `POST` | `/api/auth/validate-reset-token` | Validate reset token | No | `{ "token": "..." }` |
| `POST` | `/api/auth/reset-password` | Reset password using valid token | No | `{ "token": "...", "password": "..." }` |
| `GET` | `/api/auth/user` | Check current session & return user details | Optional (Bearer) | None |

---

## 2. User Management (`/api/users/**`)
Routed to `user-service` (Port 8083).

| Method | Endpoint | Description | Auth Required |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/users/me` | Retrieve currently logged in user profile | Bearer Token |
| `PUT` | `/api/users/profile` | Update profile information (name, picture) | Bearer Token |
| `DELETE` | `/api/users/me` | Delete current user account (requires `X-Confirm: confirm-delete`) | Bearer Token |
| `GET` | `/api/users` | List users with pagination (`?page=0&size=20&search=...`) | Bearer Token |
| `GET` | `/api/users/{id}` | Get user details by ID | Bearer Token |
| `PUT` | `/api/users/{id}/status` | Activate or deactivate user (`{ "active": true/false }`) | Bearer Token (ADMIN) |
| `PUT` | `/api/users/{id}/role` | Update user role (`{ "role": "ADMIN" }`) | Bearer Token (ADMIN) |

---

## 3. Core Business Service (`/api/core/**` or `/api/items/**`)
Routed to `core-service` (Port 8081).

| Method | Endpoint | Description | Auth Required |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/core/health` | Health check endpoint | No |
| `GET` | `/api/core/items` | List sample items | Bearer Token |
| `POST` | `/api/core/items` | Create new sample item | Bearer Token |
| `GET` | `/api/core/items/{id}` | Retrieve sample item by ID | Bearer Token |
| `PUT` | `/api/core/items/{id}` | Update sample item | Bearer Token |
| `DELETE` | `/api/core/items/{id}` | Delete sample item | Bearer Token |

---

## 4. Notifications (`/notifications/**`)
Routed to `notification-service` (Port 8084).

| Method | Endpoint | Description | Auth Required |
| :--- | :--- | :--- | :--- |
| `POST` | `/notifications/send-email` | Queue HTML transactional email | Internal / Bearer |

Payload:
```json
{
  "to": "user@example.com",
  "subject": "Welcome",
  "message": "Thank you for joining!",
  "ctaUrl": "https://myapp.com/dashboard"
}
```

---

## 5. Health & Monitoring Endpoints

All services expose Spring Boot Actuator endpoints accessible via the Gateway:

| Service | Actuator URL |
| :--- | :--- |
| API Gateway | `http://localhost:8080/actuator/health` |
| User Service | `http://localhost:8080/api/users/actuator/health` |
| Core Service | `http://localhost:8080/api/core/actuator/health` |
| Notification Service | `http://localhost:8080/notifications/actuator/health` |
| Utility Service | `http://localhost:8080/api/ai/actuator/health` |
