# Anumati

Anumati is a sector-agnostic regulatory operations platform for industrial businesses.

It helps a business understand what applies, build verified evidence, validate an application before submission, coordinate the approval lifecycle, manage compliance and renewals, and understand the regulatory effect of business changes.

Anumati is an intelligence, evidence, coordination and lifecycle layer around existing government approval ecosystems. It does not replace statutory government decisions or become another single-window portal.

## Core product model

```text
Business Regulatory Twin
        ↓
Regulatory Knowledge Fabric
        ↓
Deterministic Analysis
        ↓
Evidence Passport
        ↓
Application Pre-Flight
        ↓
Application / Department Workflow
        ↓
Inspection / Decision
        ↓
Compliance / Renewal / Incentive / Grievance
        ↓
Regulatory Change / Analytics
```

## Repository

- `frontend/` Next.js application
- `backend/` Java 21 target + Spring Boot modular monolith
- `docs/` architecture, migration, demo and production-readiness documentation

## Run locally

### Recommended: Windows without Docker

Use the supplied Windows scripts after PostgreSQL, JDK 21 or newer, Maven 3.9+, and Node.js/npm are installed. The canonical flow is `01-SETUP-ALL.cmd`, then `02-BACKEND-BUILD-AND-START.cmd`, then `03-FRONTEND-START.cmd`. If a previous backend is already running, the backend launcher reuses it; if another process owns port 8080, use `05-BACKEND-DIAGNOSTICS.cmd` and `06-STOP-BACKEND.cmd`.

### Backend only

```bash
cd backend
mvn spring-boot:run
```

Use Maven 3.9+ with Java 21 or newer; the Maven compiler target remains Java 21.

### Frontend only

```bash
cd frontend
npm install
npm run dev
```

## Frontend design

The public site and authenticated workspaces share one professional regulatory-tech design system. The hero uses a local industrial landscape asset, with restrained Framer Motion transitions and no fabricated social proof or metrics. See `docs/FRONTEND-DESIGN.md`.

## API documentation

Authenticated administrators can use `/swagger-ui.html` and `/v3/api-docs`.

## Test suite

The backend includes focused unit coverage for the operations read models and the existing core domains. The latest hardening pass adds 12 operations-service tests covering healthy paths, dependency blocking, SLA at-risk/overdue states, inspection coordination and analytics aggregation.

Run the full suite in a normal Java/Maven environment with:

```bash
cd backend
mvn test
```

## Control Tower performance

The Control Tower summary uses bulk SLA and analysis loading plus precomputed approval/dependency indexes. The prototype currently returns a complete snapshot. For statewide-scale datasets, move to paginated lanes and database-side aggregate queries or materialized operational views.

## Operations

- `/actuator/health`
- `/livez`
- `/readyz`
- `/actuator/info`
- `/actuator/prometheus`

Operational endpoints are intended to be protected in production deployments.

## Product contract

Read `docs/PROJECT-WALKTHROUGH.md` first for the repository map, frontend route map, roles, navigation model and end-to-end journey. Then use `docs/PRODUCT-SOLUTION-CONTRACT.md` for domain boundaries, `docs/DEMO-RUNBOOK.md` for the canonical prototype journey, and `docs/DEMO-VIDEO-SCRIPT-3-5-MIN.md` for the recording flow.

## Architecture principles

- PostgreSQL is the system of record.
- Documents use an S3-compatible storage contract.
- Business state and evidence are versioned.
- Analysis runs are pinned to exact business and regulatory versions.
- Application submission rejects stale analysis.
- Write-domain dependencies are acyclic.
- Internal lifecycle integration uses domain events.
- Notifications are post-commit side effects.
- Regulatory facts require verified authoritative sources.
- AI is an assistance layer, not a statutory decision-maker.
- Metrics are computed from stored records and never fabricated.


## Source-backed prototype dataset

The prototype includes a small verified Maharashtra Pollution Control Board consent chain plus a second FSSAI food-business scenario to demonstrate sector-neutral rules. See `docs/REGULATORY-DATASET.md`. Reference businesses explicitly declare regulatory characteristics; the rule engine does not infer legal applicability from a sector label alone. Administrative scrutiny tiering is explainable screening, not a statutory decision.

For judging, the database also contains a small set of seeded **reference workflow states** for applications, inspections, renewals, grievances and compliance. These records exist to make the operational workflow demonstrable from a fresh local setup; they are not presented as real statewide volume or performance metrics.

### Local document storage

The default judging profile uses a filesystem-backed object-storage adapter under `backend/.data/documents`. This keeps the document upload, extraction and consistency-check flow runnable without an external S3 account. The production adapter remains S3-compatible.


## Non-Docker Windows quick start

Prerequisites: PostgreSQL running on port 5432, JDK 21 or newer, Maven 3.9+, and Node.js 20+ with npm.

### First-time / refresh setup

1. Configure PostgreSQL using `docs/POSTGRESQL-SETUP.md`.
2. Double-click `01-SETUP-ALL.cmd`. It stays open, writes `logs\setup-last.log`, checks Java/Maven/Node/PostgreSQL, installs frontend dependencies, runs the frontend typecheck/build, and runs the complete backend Maven test suite.
3. For the local judging profile, document storage uses the filesystem-backed adapter under `backend\.data\documents`, so no cloud storage account is required.

### Every demo run

Open two Command Prompt windows.

**Window 1:**

```cmd
02-BACKEND-BUILD-AND-START.cmd
```

The launcher builds/tests the backend, then opens a separate persistent **ANUMATI Backend** window for Spring Boot. It waits for `/actuator/health` before reporting success. If startup fails, the real Spring exception remains visible in that backend window.

**Window 2:**

```cmd
03-FRONTEND-START.cmd
```

The frontend runs on `http://localhost:3000`.

### Backend startup diagnostics

If `02-BACKEND-BUILD-AND-START.cmd` fails during Spring Boot startup, keep the separate `ANUMATI Backend` window open and read the actual Spring exception shown there. Run `05-BACKEND-DIAGNOSTICS.cmd` to check PostgreSQL, port 8080 and the health endpoint.

### Health check

After both services are running:

```cmd
04-HEALTH-CHECK.cmd
```

### Demo accounts

| Role | Username | Password |
|---|---|---|
| Applicant | `applicant` | `change-me` |
| Officer | `officer` | `change-me` |
| Admin | `admin` | `change-me` |

These are local reference credentials for judging only. Change them before any non-local deployment.

### PostgreSQL

Create the local user/database once in pgAdmin Query Tool:

```sql
CREATE USER anumati WITH PASSWORD 'anumati-local';
CREATE DATABASE anumati OWNER anumati;
```

If the user already exists:

```sql
ALTER USER anumati WITH PASSWORD 'anumati-local';
```

Do not create Anumati tables manually. Flyway creates and upgrades the schema when the backend starts.

### Common local errors

- `mvn is not recognized` → add Maven's `bin` directory to Windows PATH, close existing terminals, and open a new CMD.
- `password authentication failed` → reset the password in pgAdmin with the SQL above and rerun the backend.
- `next is not recognized` → run `03-FRONTEND-START.cmd` or run `npm install` inside `frontend`.
- Document upload fails → confirm the local backend launcher sets `ANUMATI_STORAGE_MODE=local`.

For the judging flow, use `Reference Manufacturing Unit` and `demo/REFERENCE-MANUFACTURING-PROCESS.docx`.
