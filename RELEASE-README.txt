ANUMATI FINAL LOCAL RELEASE

Run order:
1. 01-SETUP-ALL.cmd
2. 02-BACKEND-BUILD-AND-START.cmd
3. 03-FRONTEND-START.cmd
4. 04-HEALTH-CHECK.cmd

Recovery / diagnostics:
- 05-BACKEND-DIAGNOSTICS.cmd = inspect PostgreSQL, port 8080 and backend health
- 06-STOP-BACKEND.cmd = stop a stale listener on port 8080 after confirmation

Backend startup behavior:
- If Anumati is already healthy on port 8080, the launcher reuses the running backend.
- If another process owns port 8080, the launcher identifies the PID and stops before starting anything new.
- The backend itself runs in a separate persistent CMD window so the real Spring Boot exception stays visible.

Local storage:
- backend\.data\documents

Demo credentials:
- applicant / change-me
- officer / change-me
- admin / change-me

Project orientation:
- Start with docs\PROJECT-WALKTHROUGH.md for the repository map, complete frontend route map, navigation model, roles and end-to-end journey.
- docs\DEMO-RUNBOOK.md = live prototype path
- docs\DEMO-VIDEO-SCRIPT-3-5-MIN.md = recording script
