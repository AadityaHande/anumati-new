# Anumati Local Windows Quick Start

## Before you run anything

Install:

- JDK 21 or newer
- Maven 3.9+
- Node.js 20+ with npm
- PostgreSQL

The current local run has been verified with Java 25.0.2, Maven 3.9.16, and PostgreSQL 18.6. The backend compiles to Java 21 bytecode.

## One-time setup

1. Configure PostgreSQL using `docs/POSTGRESQL-SETUP.md`.
2. Double-click `01-SETUP-ALL.cmd`. It relaunches itself in a persistent Command Prompt and stays open on both success and failure.
3. The local judge profile uses a filesystem-backed object store, so no S3 account is required. Files are stored under `backend\.data\documents`.
4. It checks Java/Maven/Node/npm, checks PostgreSQL on port 5432, installs frontend dependencies, builds the frontend, and runs `mvn clean package` with all backend tests.

## Every demo run

Open two Command Prompt windows in the project root.

### Window 1: backend

```cmd
02-BACKEND-BUILD-AND-START.cmd
```

This deliberately runs the complete Maven build and test suite before starting Spring Boot. Keep this window open.

Backend: `http://localhost:8080`

Health: `http://localhost:8080/actuator/health`

### Window 2: frontend

```cmd
03-FRONTEND-START.cmd
```

Frontend: `http://localhost:3000`

### Optional

```cmd
04-HEALTH-CHECK.cmd
05-BACKEND-DIAGNOSTICS.cmd
06-STOP-BACKEND.cmd
```

## Demo accounts

| Role | Username | Password |
|---|---|---|
| Applicant | `applicant` | `change-me` |
| Officer | `officer` | `change-me` |
| Admin | `admin` | `change-me` |

These are local reference credentials for the judging environment. Change them before any non-local deployment.


If backend startup fails, run `05-BACKEND-DIAGNOSTICS.cmd`. If a previous backend session is still holding port 8080, use `06-STOP-BACKEND.cmd` or inspect it with `05-BACKEND-DIAGNOSTICS.cmd`.
