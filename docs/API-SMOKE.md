# API Smoke Path

Use Swagger for interactive exploration. The following sequence is the minimum end-to-end product path.

1. `POST /api/v1/session/login`
2. `GET /api/v1/session/csrf`
3. `POST /api/v1/business-profiles`
4. `POST /api/v1/analysis/business-profiles/{id}`
5. `GET /api/v1/readiness?businessProfileId={id}&analysisRunId={analysisRunId}`
6. `GET /api/v1/business-profiles/{id}/evidence/passport`
7. `GET /api/v1/preflight/business-profiles/{id}/analysis/{analysisRunId}`
8. `POST /api/v1/applications`
9. `GET /api/v1/applications/{id}/timeline`
10. Department transition/query/inspection/decision endpoints as appropriate
11. `GET /api/v1/knowledge/applications/{id}/evidence-graph`
12. `GET /api/v1/operations/control-tower`
