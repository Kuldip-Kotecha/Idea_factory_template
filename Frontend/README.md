# Idea Factory - Angular Frontend

This directory contains the client-side application for the Idea Factory platform template. It is built with **Angular 18** and styled with **TailwindCSS**, providing a modern, responsive, and secure foundation for full-stack applications.

## 🛠️ Tech Stack

- **Framework**: Angular 18 (Standalone Components)
- **Styling**: TailwindCSS with modern dark/light UI
- **State Management**: RxJS & Reactive Signals
- **Routing**: Angular Router with Auth & Role Guards
- **HTTP Client**: Angular HttpClient with JWT Interceptors
- **Authentication**: JWT & Google OAuth2 SSO integration

## ✨ Features

- **Authentication & Authorization**: Pre-configured login, signup, password reset, email verification, and Google OAuth2 login.
- **Role-Based Access Control**: Route guards (`auth.guard.ts`, `role.guard.ts`) and role-based UI conditional rendering (`ROLE_USER`, `ROLE_ADMIN`).
- **User Profile Management**: User profile view and update screens.
- **Empty Dashboard & Extensible Slots**: Ready-to-implement dashboard cards, metrics, and navigation placeholders for business features.
- **API Communication & Resilience**: Centralized HTTP services, environment configs, and health-check monitoring.

## 📋 Prerequisites

- **Node.js**: v18 or higher
- **NPM**: v9 or higher

## 🚀 Installation

1. Navigate to the frontend directory:
    ```bash
    cd Frontend
    ```
2. Install dependencies:
    ```bash
    npm install
    ```

## 💻 Development Server

Run the application in development mode:

```bash
npm start
```

Navigate to `http://localhost:4200/`. The application will automatically reload if you change any of the source files.

> **Note**: This application expects the API Gateway to be running at `http://localhost:8080`. Start the backend services first.

## 🏗️ Build

To build the project for production:

```bash
npm run build
```

The build artifacts will be stored in the `dist/` directory.

## 🧪 Testing

Execute unit tests via [Karma](https://karma-runner.github.io):

```bash
npm test
```

## 📁 Project Structure

- `src/app/pages/` — Application pages (Dashboard, Login, Signup, Profile, Admin Users, Password Reset)
- `src/app/services/` — API services (`AuthService`, `UserService`, `AdminService`)
- `src/app/components/` — Shared, reusable UI components (Brand Logo, Modals, Tables)
- `src/app/guards/` — Route guards for authentication and authorization
- `src/environments/` — Environment configurations (`environment.ts`, `environment.prod.ts`)
