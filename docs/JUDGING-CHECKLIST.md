# Anumati Judging Checklist

## What is actually working

- PostgreSQL-backed business profiles and profile versions.
- Version-pinned regulatory analysis runs.
- Verified-source regulatory catalogue and deterministic rule evaluation.
- Document requirements scoped to the current analysis.
- Real local document storage through the same object-storage interface used by the production S3 adapter.
- Document upload, extraction and business-profile consistency checks.
- Evidence promotion and version-aware reuse.
- Application lifecycle state machine.
- Blocking approval dependencies.
- SLA calculation and warning/overdue status.
- Queries, responses, inspection scheduling/completion and decisions.
- Department control tower, inspection planning, renewals, grievances and compliance.
- Notifications and audit events.
- Regression test suite: the verified local backend run completed with 40 tests, 0 failures and 0 errors.

## Reference data boundary

The local database contains a small source-backed regulatory catalogue and a seeded set of reference workflow states so a fresh judging environment has something meaningful to operate on.

Those reference records are not claims about statewide application volume, government performance, or real customer activity.

## Safe claims during judging

Say:

> "This prototype demonstrates the regulatory operations layer and workflow. The statutory government system remains the system of record for the final legal decision."

Say:

> "The prototype uses a deliberately small verified dataset to demonstrate the rule model. The architecture is designed for broader catalogue coverage."

Say:

> "Document extraction and consistency checking are implemented locally for the prototype, with an S3-compatible storage adapter retained for production deployment."

Do not say:

- "We have integrated every Maharashtra department."
- "AI approves or rejects applications."
- "The prototype guarantees faster approvals."
- "These seeded records are real government applications."
- "This database represents statewide application volume."
