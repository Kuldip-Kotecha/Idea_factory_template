# InterviewCoach - Angular Frontend

This directory contains the client-side application for the InterviewCoach platform. It is built with **Angular 18** and styled with **TailwindCSS**, providing a modern, responsive user interface for AI-driven mock interviews.

## 🛠️ Tech Stack

- **Framework**: Angular 18
- **Styling**: TailwindCSS
- **State Management**: RxJS
- **Charts**: Chart.js / ng2-charts
- **HTTP Client**: Angular HttpClient

## ✨ Features

- **Interactive Wizard**: Step-by-step setup for interview sessions.
- **Audio/Video Interface**: Real-time interaction with the AI interviewer using Web Audio API.
- **Dashboard**: Visual progress tracking and recent activity.
- **Reports**: Detailed breakdown of interview performance with scoring.
- **Profile & Onboarding**: Resume upload, AI parsing, and target role configuration.

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

> **Note**: This application expects the backend API to be running at `http://localhost:8080`. Start the backend from [`Backend/`](../Backend) first.

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

- `src/app/pages/` — Core application pages (Dashboard, Wizard, Interview, Report, Profile)
- `src/app/services/` — API services for communicating with the backend
- `src/app/components/` — Shared UI components
- `src/environments/` — Environment configurations (API URLs, keys)
- `AGENTS.md` — Frontend development conventions
- `UX-PRINCIPLES.md` — UX design principles
