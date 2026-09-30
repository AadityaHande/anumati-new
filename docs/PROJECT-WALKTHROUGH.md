# Anumati Project Walkthrough

This is the single orientation document for a new teammate, judge, reviewer, or future maintainer who needs to understand Anumati without reading the whole repository first.

## 1. What Anumati is

Anumati is a regulatory operations platform for industrial businesses. It sits around existing government approval ecosystems and connects:

```text
Business facts
    ↓
Regulatory applicability
    ↓
Source + rule trace
    ↓
Evidence readiness
    ↓
Pre-flight checks
    ↓
Application workflow
    ↓
Scrutiny / query / inspection / decision
    ↓
Compliance / renewals / support / grievances
```

The product is not presented as a government authority and does not replace statutory decisions.

## 2. Repository map

```text
anumati/
├── frontend/                 Next.js applicant, officer and admin UI
│   ├── app/                  Routes and pages
│   ├── components/           Shared shell, brand, icons and motion
│   ├── lib/api.ts            Typed frontend API client
│   └── public/               Static assets and favicon
│
├── backend/                  Spring Boot modular monolith
│   ├── src/main/java/        Domain modules and APIs
│   └── src/main/resources/   Flyway migrations and application config
│
├── demo/                     Reference document used by the local demo
├── docs/                     Architecture, runbook, dataset and design docs
└── 0*.cmd                    Windows setup / start / health scripts
```

## 3. Frontend route map

### Public

```text
/                       Landing page
/login                  Applicant / officer / admin login
/privacy                Privacy information
/terms                  Terms and usage boundary
```

### Applicant workspace

```text
/app                                      Workspace overview
/app/profiles/new                         Create business profile
/app/profiles/:id                         Regulatory map + current analysis
/app/profiles/:id/twin                    Business regulatory twin
/app/profiles/:id/history                 Business state history
/app/profiles/:id/edit                   Create next business version
/app/profiles/:id/preflight               Application pre-flight
/app/profiles/:id/evidence                Evidence passport
/app/profiles/:id/documents               Document preparation and extraction
/app/profiles/:id/applications            Application desk
/app/profiles/:id/impact                  Regulatory impact simulation
/app/profiles/:id/incentives              Government support schemes
/app/profiles/:id/renewals                Renewal records
/app/profiles/:id/compliance              Compliance register
/app/profiles/:id/grievances              Grievance record
/app/notifications                        Action centre
/app/applications/:id                    Application record and timeline
/app/applications/:id/evidence-graph     Traceability graph
/app/knowledge/approvals/:id             Approval-level regulatory trace
```

### Department workspace

```text
/officer                                 Control tower
/officer/applications/:id                Officer application review
/officer/inspections                     Inspection planning
/officer/renewals                        Renewal desk
/officer/grievances                      Grievance desk
```

### Administration

```text
/admin/regulatory                       Verified regulatory catalogue
```

## 4. Navigation model

The authenticated shell has one fixed rail and one content pane.

- **Overview** is active only on `/app`.
- **Regulatory map** is active on the selected profile root and its business-twin view.
- **Application pre-flight** is active on its own route family.
- **Evidence passport** is active on its own route family.
- Lifecycle items are active only for their matching profile route family.
- **Notifications** is a standalone action-centre destination.
- Department sub-pages do not leave **Control tower** incorrectly highlighted.
- The rail can collapse to an icon-only state on desktop and open/close on smaller screens.
- The business switcher is a deliberate menu rather than a native browser select so its appearance stays consistent across platforms.

## 5. Applicant journey

### Step 1: Choose a reference business

Use `Reference Manufacturing Unit` for the primary local judging flow.

### Step 2: Run regulatory analysis

The backend evaluates the stored business attributes against active, source-backed rules and persists an analysis run.

The UI should show:

- applicable / conditional / not applicable result
- reason
- rule code
- source title / source link
- scrutiny tier where present

### Step 3: Prepare evidence

Documents are derived from the current analysis. The local judging profile can store files under `backend/.data/documents` so the document flow does not require an external S3 account.

The supplied demo file is:

```text
demo/REFERENCE-MANUFACTURING-PROCESS.docx
```

The intended path is:

```text
Requirement
  ↓
Upload
  ↓
Extraction
  ↓
Consistency check
  ↓
Review
  ↓
Verified evidence
```

### Step 4: Pre-flight

Pre-flight checks are evaluated against the exact business profile and analysis run. The purpose is to expose missing evidence and blocking dependencies before submission.

### Step 5: Regulatory impact

The simulator accepts a proposed business state and compares it with the saved profile. It does not mutate the saved profile.

### Step 6: Application

A ready approval can be submitted against the analysis run. The application stays attached to the relevant business and regulatory versions.

### Step 7: Traceability

The application record links its analysis snapshot and evidence graph so a reviewer can trace the chain instead of treating the application as an isolated status row.

## 6. Officer journey

Use the seeded officer records after switching to the officer role.

The Control Tower is the main entry point:

```text
Application lane
  ├── current state
  ├── SLA state
  ├── blockers
  └── next action
```

From there, the officer can open an application and review query, inspection and decision information. Separate desks handle inspection planning, renewals and grievances.

## 7. Regulatory knowledge boundary

The prototype deliberately uses a scoped, source-backed regulatory dataset. The rule engine does not claim statewide coverage.

The important contract is:

```text
Verified source
      ↓
Rule / approval configuration
      ↓
Business facts
      ↓
Deterministic applicability result
```

AI can support document extraction or knowledge assistance, but statutory applicability is not delegated to an opaque generative decision.

## 8. Roles

| Role | Main experience |
| --- | --- |
| Applicant | Business profile, regulatory analysis, evidence, pre-flight, applications, compliance, support and grievances |
| Department officer | Control tower, application review, inspection planning, renewals and grievances |
| Admin | Verified regulatory catalogue and source boundary |

Local reference credentials are documented in the root README and local quick-start guide.

## 9. Local run

The canonical Windows flow is:

```text
01-SETUP-ALL.cmd
        ↓
02-BACKEND-BUILD-AND-START.cmd
        ↓
03-FRONTEND-START.cmd
        ↓
04-HEALTH-CHECK.cmd
```

If the backend port is occupied:

```text
05-BACKEND-DIAGNOSTICS.cmd
06-STOP-BACKEND.cmd
```

PostgreSQL is the system of record. Flyway creates and upgrades the schema on backend startup; tables should not be created manually.

## 10. What to show in a judging demo

Keep the video centered on one business story:

```text
Business facts
→ Analysis
→ Source / reason
→ Evidence
→ Pre-flight
→ Impact simulation
→ Application
→ Officer Control Tower
```

Use the supplied reference document rather than an improvised file. Do not call seeded records "dummy data" in the presentation; use "reference workflow" or "seeded reference scenario".

## 11. What not to claim

Do not claim:

- complete Maharashtra regulatory coverage
- guaranteed statutory approval-time reduction
- legal certification of uploaded documents
- autonomous government decision-making
- that reference workflow records are real statewide statistics

## 12. First files to read

For a fast orientation, read these in order:

1. `README.md`
2. `docs/PROJECT-WALKTHROUGH.md` (this file)
3. `docs/ARCHITECTURE.md`
4. `docs/PRODUCT-SOLUTION-CONTRACT.md`
5. `docs/DEMO-RUNBOOK.md`
6. `docs/DEMO-VIDEO-SCRIPT-3-5-MIN.md`
7. `docs/REGULATORY-DATASET.md`
8. `docs/POSTGRESQL-SETUP.md`

## Frontend navigation map (V13)

The frontend is split into a public product site, an applicant workspace, and a department operations workspace. The applicant rail is fixed and collapsible; the department rail is fixed and separately collapsible. Deep pages expose deterministic parent navigation so operators never have to rely on browser history to find their way back.

See `docs/FRONTEND-WALKTHROUGH.md` for the complete route map, interaction rules and recording path.
