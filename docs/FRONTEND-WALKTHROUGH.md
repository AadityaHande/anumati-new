# Anumati frontend walkthrough

## 1. Product surfaces

### Public site
- `/` - product story and entry points
- `/privacy` - privacy notice
- `/terms` - terms
- `/login` - role-aware sign in

### Applicant workspace
- `/app` - business overview and current work
- `/app/profiles/new` - create business profile
- `/app/profiles/:id` - regulatory map / business regulatory twin
- `/app/profiles/:id/twin` - detailed regulatory twin
- `/app/profiles/:id/edit` - edit business state
- `/app/profiles/:id/preflight` - submission readiness
- `/app/profiles/:id/documents` - evidence upload and document analysis
- `/app/profiles/:id/evidence` - verified evidence passport
- `/app/profiles/:id/applications` - business applications
- `/app/profiles/:id/impact` - proposed-change simulation
- `/app/profiles/:id/incentives` - support scheme matching
- `/app/profiles/:id/renewals` - renewal register
- `/app/profiles/:id/compliance` - compliance register
- `/app/profiles/:id/grievances` - business grievances
- `/app/profiles/:id/history` - profile/version history
- `/app/notifications` - actions that need attention

### Department workspace
- `/officer` - control tower
- `/officer/applications/:id` - application review and statutory workflow actions
- `/officer/inspections` - inspection coordination
- `/officer/renewals` - renewal desk
- `/officer/grievances` - grievance desk

## 2. Navigation rules

The applicant workspace uses a fixed rail. The rail can be collapsed on desktop and opened as a drawer on smaller screens. A top-bar back control is shown on deeper routes and returns to the nearest stable parent route rather than relying on browser history.

The department workspace uses its own fixed operational rail. From a deep department page, the top bar shows a deterministic back link to Control Tower. `Applicant workspace` is always available as a role transition link.

Visible arrows indicate navigation. Action rows that do not navigate should not display a misleading arrow.

## 3. Applicant flow

`Overview -> Regulatory map -> Analysis -> Pre-flight -> Documents -> Evidence passport -> Applications -> Impact -> Continue`

The business profile is the source context for the workflow. The same profile id is carried through the deep routes so the user does not lose the selected business.

## 4. Department flow

`Control tower -> urgent queue -> application review -> query / inspection / decision`

Secondary desks branch from Control Tower:
- Inspection planning
- Renewal desk
- Grievance desk

The Control Tower exposes operational state; statutory decisions remain inside the authorised department workflow.

## 5. Interaction conventions

- Primary actions are solid, high-contrast buttons.
- Secondary actions use quiet outlined controls.
- Interactive rows provide hover feedback without floating-card animation.
- Status is represented by restrained pills and dots, not oversized badges.
- Panels use small, consistent corner radii and mostly rely on borders and whitespace for separation.
- The product avoids decorative AI-style gradients, glowing surfaces and uniform card grids.
- Motion is short and functional; users should always understand what moved and why.

## 6. Demo-critical path

For a 3-5 minute recording, use the Reference Manufacturing Unit and the supplied reference manufacturing process document.

Recommended path:

`Landing -> Sign in -> Reference Manufacturing Unit -> Regulatory analysis -> Source trace -> Documents -> Upload -> Pre-flight -> Regulatory impact -> Application -> Control Tower`

Do not demonstrate setup screens, database tools, Swagger, terminals or empty modules.
## Role-based navigation

Anumati has three presentation contexts tied to the authenticated session returned by the backend:

- **Applicant** → `/app`
- **Department officer** → `/officer`
- **Admin** → `/admin`

The backend intentionally grants the admin account both `ADMIN` and `APPLICANT` authorities so admin API capabilities can access business-facing data where needed. The frontend therefore uses the `ADMIN` authority as the primary landing context and provides explicit links to Department operations and the Applicant workspace rather than mixing all three experiences into one default screen.

Login routing is derived from the roles returned by `/api/v1/session/login`; no password or username is used to infer a role.

