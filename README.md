# Institute Workforce Tracking System

A full-stack system for tracking institute workforce and attendance.
This repository currently contains the **backend foundation** — a
production-grade architectural skeleton onto which feature modules
(authentication, employee management, attendance) are plugged in without
altering the underlying structure.

> **Project status:** Foundation milestone complete. No business features
> (auth, employees, attendance) are implemented yet — by design. The
> foundation is intentionally built first so later modules drop in cleanly.

---

## Table of Contents
- [Overview](#overview)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Backend Folder Structure](#backend-folder-structure)
- [Getting Started](#getting-started)
- [Configuration & Profiles](#configuration--profiles)
- [API](#api)
- [Standard Response Format](#standard-response-format)
- [Future Modules](#future-modules)

---

## Overview

The Institute Workforce Tracking System will manage employees and their
attendance for an institute. This milestone establishes the **foundation**:
a layered Spring Boot backend with centralized error handling, a uniform API
response contract, a stateless security architecture prepared for JWT, and a
health endpoint — all wired and verified end-to-end.

**What the foundation provides today**
- A uniform success/error response envelope returned by every endpoint.
- Global exception handling that converts any error into that envelope.
- A stateless Spring Security filter chain (public + protected routes)
  prepared for JWT, with JSON 401 responses.
- Externalized, profile-based configuration (dev / prod).
- Time-zone-aware date utilities (critical for attendance correctness).
- A verified `GET /api/health` endpoint.

---

## Tech Stack

### Backend
| Technology | Purpose |
| --- | --- |
| Java 24 | Language runtime |
| Spring Boot 4.1.x | Application framework |
| Spring Web (MVC) | REST controllers |
| Spring Security | Security filter chain (JWT-ready) |
| Spring Data JPA | Persistence layer (Hibernate) |
| MySQL 8 | Relational database |
| jjwt 0.12.x | JWT signing material (auth milestone) |
| Spring Mail | Email notifications (future) |
| Spring WebSocket | Real-time updates (future) |
| Spring Actuator | Operational health/metrics |
| Lombok | Boilerplate reduction |
| Maven | Build tool |

### Frontend (planned — separate phase)
React 19 · Vite · React Router · Axios · Material UI · React Context API

---

## Architecture

A strict layered architecture. Each layer depends only on the one beneath it,
and business logic lives exclusively in the service layer.

```
Controller  ->  Service  ->  Repository  ->  Database
   (HTTP)      (business)    (data access)    (MySQL)
```

- **Controllers** handle the HTTP concern only (routing, delegating, shaping
  the response). No business logic.
- **Services** hold business logic (added per feature module).
- **Repositories** handle data access via Spring Data JPA.
- Cross-cutting concerns (security, error handling, response format) are
  centralized so feature modules inherit them for free.

Guiding principles: SOLID, clean architecture, constructor injection only
(no field injection), and REST best practices.

---

## Backend Folder Structure

```
src/main/java/com/institute/workforce_tracking
├── config          # Cross-cutting beans: WebConfig (CORS), AppConfig (PasswordEncoder)
├── constants       # AppConstants, ApiConstants
├── controller      # REST controllers (HealthController)
├── dto             # Data Transfer Objects (payloads, e.g. HealthResponse)
├── enums           # Shared enumerations (future)
├── exception       # BusinessException, custom exceptions, GlobalExceptionHandler
├── mapper          # Entity <-> DTO mappers (future)
├── notification    # Email / push notifications (future)
├── repository      # Spring Data JPA repositories (future)
├── request         # Inbound request DTOs (future)
├── response        # ApiResponse, ErrorResponse (uniform envelopes)
├── scheduler       # Scheduled jobs (future)
├── security        # SecurityConfig, JwtUtil, JwtAuthenticationFilter, entry point
├── service         # Service interfaces (future)
│   └── impl        # Service implementations (future)
├── util            # DateTimeUtil, common helpers
├── validation      # Custom validators (future)
├── websocket       # WebSocket endpoints/handlers (future)
└── WorkforceTrackingApplication.java   # Entry point
```

> Some packages are created as part of the scalable structure and are
> populated in later milestones. This is intentional — the skeleton exists so
> features have a defined home.

```
src/main/resources
├── application.properties          # Base config + active profile selector
├── application-dev.properties      # Local development overrides
├── application-prod.properties     # Production overrides (env-driven secrets)
└── banner.txt                      # Startup banner
```

---

## Getting Started

### Prerequisites
- **JDK 24**
- **MySQL 8** running locally
- **Maven** (or use the bundled `./mvnw` wrapper)

### 1. Database
No manual database creation is needed in development — the dev JDBC URL uses
`createDatabaseIfNotExist=true`, so the `workforce_tracking` schema is created
on first run. Ensure MySQL is running and the credentials in
`application-dev.properties` match your local setup.

### 2. Run the backend
```bash
# Windows PowerShell
./mvnw spring-boot:run

# macOS / Linux
./mvnw spring-boot:run
```
The application starts on **http://localhost:8080** with context path `/api`.

### 3. Verify
```bash
curl http://localhost:8080/api/health
```
Expected:
```json
{
  "success": true,
  "message": "Service is healthy",
  "data": { "status": "UP" },
  "timestamp": "2026-07-14T09:00:00Z"
}
```

### Frontend
The React frontend is a **separate, upcoming phase**; run instructions will be
added when it is scaffolded.

---

## Configuration & Profiles

Configuration is externalized and profile-based. The base
`application.properties` selects the active profile (default `dev`) and holds
values shared across environments. Profile files override per environment.

| Profile | Purpose | Notable behavior |
| --- | --- | --- |
| `dev` | Local development | `ddl-auto=update`, SQL logging, verbose logs |
| `prod` | Production | `ddl-auto=validate`, env-driven secrets, lean logs |

**Secrets are never committed.** Production reads `JWT_SECRET`, `DB_URL`,
`DB_USERNAME`, `DB_PASSWORD`, and mail credentials from environment variables.

Switch profiles at runtime:
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
# or set SPRING_PROFILES_ACTIVE=prod in the environment
```

---

## API

| Method | Path | Auth | Description |
| --- | --- | --- | --- |
| GET | `/api/health` | Public | Application liveness check |
| GET | `/api/actuator/health` | Public | Actuator infrastructure health |

All other paths currently return a JSON `401` until authentication is
implemented — the secure-by-default posture is intentional.

---

## Standard Response Format

Every endpoint returns one of two shapes.

**Success**
```json
{
  "success": true,
  "message": "Service is healthy",
  "data": { "status": "UP" },
  "timestamp": "2026-07-14T09:00:00Z"
}
```

**Error**
```json
{
  "success": false,
  "message": "Authentication is required to access this resource.",
  "error": "UNAUTHORIZED",
  "status": 401,
  "path": "/api/some-protected-route",
  "timestamp": "2026-07-14T09:00:00Z"
}
```

---

## Future Modules

Planned milestones, each designed to plug into this foundation without
architectural change:

- [ ] **Authentication & Authorization** — JWT login, roles, token refresh.
- [ ] **Employee Management** — CRUD for institute staff.
- [ ] **Attendance Tracking** — clock-in/out, daily records, corrections.
- [ ] **Scheduling** — automated daily/monthly attendance summaries.
- [ ] **Notifications** — email + real-time (WebSocket) alerts.
- [ ] **Reporting** — attendance reports and exports.
- [ ] **Frontend** — React 19 + Vite + Material UI client.

---

## License
Educational project — built for learning enterprise-grade full-stack
development.