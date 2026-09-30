# Anumati Final Submission Video - 4 Minute Script

## Purpose

Show one coherent, working business-to-department journey. The recording should demonstrate real application state, real database-backed transitions, deterministic regulatory analysis, evidence handling, and traceability.

Do not try to show every screen.

## Final story

Business facts
→ regulatory analysis
→ evidence requirements
→ document upload and extraction
→ pre-flight
→ application lifecycle
→ department control tower
→ traceability

## Recording setup

Use:

- 1920×1080 recording if possible.
- Browser zoom at 90–100%.
- Only the Anumati browser window visible.
- No DevTools, terminal or database window visible in the recording.
- Mouse movement should be deliberate. Move, click, pause for 1–2 seconds, then continue.
- Record microphone narration separately if possible so a missed click can be edited without rerecording the voice.

Before recording:

1. Start PostgreSQL.
2. Run `02-BACKEND-BUILD-AND-START.cmd` and wait for `Started AnumatiApplication`.
3. Run `03-FRONTEND-START.cmd`.
4. Run `04-HEALTH-CHECK.cmd` once if desired, then close it.
5. Open `http://localhost:3000`.
6. Log in as `applicant / change-me`.
7. Use `Reference Manufacturing Unit` as the primary scenario.
8. Keep `demo/REFERENCE-MANUFACTURING-PROCESS.docx` ready for the document step.

The database also contains reference application states for the officer workflow, so the control tower is populated without inventing performance statistics.

---

## 0:00–0:25 — Problem and product

### Screen
Landing page.

### Say

> "Industrial businesses do not just need a list of approvals. They need to understand what applies to their business, prepare the right evidence, clear dependencies, submit correctly, and then follow the application through scrutiny, inspection and decision. Anumati is the operational layer connecting those steps around the government's statutory systems."

Click **Get started** or **Sign in**.

---

## 0:25–0:55 — Start from business facts

### Screen
Applicant workspace → `Reference Manufacturing Unit`.

Pause on the business facts.

### Say

> "Anumati starts from the current business state. The profile captures the facts that influence regulatory applicability, and every later analysis is tied to this saved version."

Point to:

- sector
- activity
- district
- investment
- employees
- business stage

Do not spend time reading every field.

---

## 0:55–1:30 — Regulatory analysis

### Action
Open **Regulatory map** and click **Run analysis**.

### Show

- MPCB Consent to Establish
- MPCB Consent to Operate
- applicability status
- explanation / reason
- rule code
- source link / trace

### Say

> "The regulatory engine evaluates the business facts against the configured rules. The result is not an opaque AI score. It tells us what applies, why it applies, and which verified source and rule support that result."

Click **Trace** once.

### Say

> "This keeps the regulatory reasoning auditable instead of hiding it behind a generic recommendation."

Return to the analysis.

---

## 1:30–2:05 — Evidence and document intelligence

### Action
Open **Documents**.

If the page says analysis is required, click **Run analysis** once. The current checklist should then load.

### Show

- requirement categories
- mandatory evidence
- current readiness state

Select **MANUFACTURING_PROCESS**.

Choose:

`demo/REFERENCE-MANUFACTURING-PROCESS.docx`

Click **Upload and check**.

### Say

> "The evidence workspace is driven by the current regulatory analysis. This is not a generic file bucket. The business gets the evidence categories associated with the approvals that were actually found applicable."

After upload completes, click **Analyse** if it is not already shown as processed.

### Say

> "The document is stored, extracted, and checked against the business profile. A matching result can then become reusable verified evidence."

If the screen shows the match, briefly point to the business name and district consistency checks.

---

## 2:05–2:35 — Pre-flight before submission

### Action
Open **Application Pre-Flight**.

### Show

- readiness score
- blocker / warning counts
- evidence gaps
- dependency state

### Say

> "Before a submission enters the government workflow, Anumati runs a pre-flight check. The purpose is simple: catch missing evidence and blocking conditions before the application becomes someone else's exception to resolve."

Do not manually fix every requirement in the video. One clearly visible blocker or warning is enough.

---

## 2:35–3:00 — Regulatory impact

### Action
Open **Regulatory Impact**.

Change the proposed business stage from **SETUP** to **OPERATING** without saving.

### Say

> "The same regulatory model can be used before a business change is made. Here we can test a proposed operating-state change and see the regulatory delta without mutating the saved profile."

Pause on the changed approval result.

---

## 3:00–3:30 — Applicant lifecycle and traceability

### Action
Open **Applications**.

Open one application that has a populated reference state, if available.

Show the timeline briefly.

### Say

> "Applications are version-linked and stateful. Queries, responses, inspections and decisions are recorded against the application instead of being treated as disconnected tasks."

Then open **History** briefly.

### Say

> "Business changes create new profile versions, so an earlier analysis does not silently change underneath an application."

---

## 3:30–3:55 — Department control tower

### Action
Sign out and log in as `officer / change-me`.

Open **Control tower**.

### Show

- application count
- pending scrutiny
- query awaiting applicant
- SLA warning / overdue state
- inspection pending
- open grievance / compliance due
- application lanes

Click one lane that is in `QUERY_RAISED` or `INSPECTION_SCHEDULED`.

### Say

> "On the department side, the same records become an operational control tower. Officers can see where work is, what is waiting, which service timelines need attention, and which dependencies are blocking progress."

Do one real workflow action only if it is already prepared, such as scheduling/completing an inspection or responding to the seeded query.

---

## 3:55–4:15 — Close

Return to the main dashboard or regulatory map.

### Say

> "Anumati connects business state, source-backed regulatory reasoning, evidence, pre-flight checks and application operations in one traceable workflow. The platform supports statutory decisions; it does not replace them."

End on the product interface, not on a slide full of claims.

---

# What must be visible at least once

- Business profile facts
- Deterministic applicability result
- Reason + source trace
- Document requirement checklist
- Real document upload
- Extraction / consistency result
- Pre-flight state
- Regulatory impact delta
- Application timeline
- Department control tower

# What not to show

- Terminal windows
- Database tables
- Swagger as part of the main story
- Empty screens unless the empty state itself proves a design point
- Fabricated statistics
- Fake user counts, savings, approval rates or performance claims
- A screen labelled "dummy data"
- Any claim that the prototype covers every Maharashtra approval
- Any claim that AI makes the statutory approval decision

# If a feature fails during recording

Do not improvise a fake result.

Cut to the last successful state and explain the capability at a high level, or record that section separately after fixing the local setup.
