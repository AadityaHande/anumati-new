# Anumati Architecture Contract

## Product boundary
Anumati is a sector-agnostic regulatory operations platform that works around existing government approval ecosystems. It does not replace statutory decision-making or become a new single-window portal.

The product contract is:

**Understand → Prepare → Execute → Continue**

- Understand: regulatory applicability, sources, incentives and regulatory impact.
- Prepare: evidence passport, document readiness and application pre-flight.
- Execute: application workflow, cross-department coordination, queries, inspections and decisions.
- Continue: compliance, renewals, alerts, grievances and regulatory change impact.

The detailed target solution is defined by the submitted Anumati architecture document. fileciteturn3file0L13-L25

## Core architecture

```text
Next.js
  ↓
Spring Boot modular monolith
  ↓
PostgreSQL
  ├── Business Regulatory Twin
  ├── Regulatory Knowledge Fabric
  ├── Applicability / Analysis
  ├── Evidence / Documents
  ├── Application Lifecycle
  ├── Operations / SLA / Notifications
  └── Compliance / Renewals / Incentives / Grievances

S3-compatible object storage
  ↓
Production: Amazon S3
```

## Domain boundaries

The current package boundaries represent bounded contexts rather than microservices. The system stays a modular monolith until operational scale justifies separation.

### Command domains
- `business`: current regulatory state and version history of a business.
- `regulatory`: sources, approvals, rules and rule conditions.
- `analysis`: deterministic rule evaluation and immutable analysis snapshots.
- `dependency`: approval dependency graph.
- `document`: document lifecycle and document intelligence.
- `evidence`: verified reusable business facts.
- `readiness`: document requirement and readiness evaluation.
- `preflight`: submission gate and application readiness.
- `application`: submission, scrutiny, queries, inspections and decisions.
- `sla`: service-level configuration and due-state computation.
- `renewal`: approval renewal lifecycle.
- `compliance`: ongoing obligations.
- `incentive`: support-scheme eligibility.
- `grievance`: grievance lifecycle.

### Read and coordination domains
- `knowledge`: read-only Regulatory Knowledge Fabric trace across sources, rules, documents and dependencies.
- `analytics`: government-side operational aggregation.
- `notification`: post-commit in-app notification handling and operational scheduler.
- `impact`: hypothetical regulatory impact simulation.
- `audit`: append-only audit events.

## Dependency rule

The architecture must remain acyclic at package level.

Rules:

1. Domain modules do not depend on `analytics`, `knowledge` or other read-side facades.
2. Lifecycle side effects such as SLA due-date attachment and renewal creation are triggered by domain events rather than direct Application-to-SLA/Renewal dependencies.
3. Notifications are post-commit consumers of domain events and must never determine the success of a statutory transaction.
4. Storage configuration never reaches back into document-domain configuration.
5. Cross-domain read aggregation belongs in `knowledge` or `analytics`, not in the write domain that owns the entity.
6. A module owns its write model and repository. Other modules consume a service/facade or a stable event contract rather than reaching into another module's persistence implementation where avoidable.

The source tree is organized as a modular monolith with package boundaries enforced by code review and the module dependency rules documented below.

## Business Regulatory Twin

`BusinessProfile` remains the persistence aggregate for the current state. `BusinessProfileVersion` stores immutable JSON snapshots for every profile version.

This allows:

```text
Profile v1 → analysis v1 → evidence v1
Profile v2 → analysis v2 → evidence v2
```

An application must never be submitted against an analysis run older than the current business profile version.

## Regulatory Knowledge Fabric

The knowledge model is relational, not a premature graph database:

```text
Source
  ↓
Rule / condition
  ↓
Approval / obligation / scheme
  ↓
Document requirement / evidence
  ↓
Dependency
```

PostgreSQL remains the system of record. Recursive queries, indexed adjacency tables and read-side projections are sufficient for the current scale. The source document explicitly recommends PostgreSQL rather than adding Neo4j only because the model contains relationships. fileciteturn3file0L116-L166

## Event model

Internal domain events are used for lifecycle integration inside the monolith:

```text
Application submitted
    ├── SLA due date handler
    └── notification handler (after commit)

Query raised / response
    └── notification handler (after commit)

Inspection scheduled
    └── notification handler (after commit)

Decision recorded
    ├── renewal handler
    └── notification handler (after commit)

Grievance created
    └── notification handler (after commit)
```

Events that enforce transactional business invariants run in-process in the same transaction. Notification handlers run after commit. An outbox is a production requirement when external delivery is introduced.

## Trust model

- Official verified sources are the regulatory authority.
- Applicability is deterministic and explainable.
- Business state is versioned.
- Evidence is versioned against business state.
- Analysis snapshots reference the exact business version and rule-set version.
- AI, when added, retrieves/explains verified material and does not make statutory decisions.

## Production boundary

- OIDC/Cognito for identity.
- RDS PostgreSQL.
- Amazon S3.
- CloudFront / WAF / rate limiting at the edge.
- CloudWatch and Prometheus-compatible metrics.
- Outbox/event delivery for external notification integrations.
- Malware/content scanning for uploads.
- Automated integration tests and restore drills.
