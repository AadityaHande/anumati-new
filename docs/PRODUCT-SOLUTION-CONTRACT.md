# Anumati Product and Solution Contract

Anumati is a sector-agnostic regulatory operations platform for industrial businesses. It is an intelligence, evidence, coordination and lifecycle layer around existing government approval ecosystems. It does not replace statutory authorities or become another single-window portal.

## Core promise

```text
Business state
  -> regulatory understanding
  -> verified evidence
  -> application pre-flight
  -> coordinated workflow
  -> decision
  -> compliance and renewal
  -> impact and regulatory change intelligence
```

## Core differentiators

1. Evidence Passport: reusable, versioned business facts backed by source documents and verification history.
2. Application Pre-Flight: deterministic readiness validation before submission.
3. Regulatory Impact Simulator: compare a proposed business state with the current state without mutating the real profile.
4. Application Evidence Graph: trace requirement -> rule -> evidence -> source -> workflow outcome.
5. Cross-Department Control Tower: expose blockers, ownership, SLA state and next actions across an application.
6. Regulatory Change Intelligence: identify rule/source changes and the approvals, obligations and businesses that may be affected.

## Domain model

Identity / Access, Business Regulatory Twin, Regulatory Catalogue, Rule Engine, Analysis, Evidence, Documents, Pre-Flight, Dependencies, Applications, Department Workflow, Queries, Inspections, Decisions, SLA / Escalation, Compliance, Renewals, Incentives, Grievances, Notifications, Regulatory Change, Analytics, Regulatory Assistant, Audit.

## Architecture rule

Keep one modular monolith while these domains share a transactional PostgreSQL system of record. Domain dependencies are one-way. Cross-domain side effects use internal domain events. External delivery must not become a dependency of statutory workflow transactions.

## Regulatory trust rule

Only verified authoritative sources can drive applicability rules, document requirements, SLAs, incentives or compliance obligations. AI may retrieve and explain verified material but must not make statutory decisions.

## Sector scope

The platform is sector agnostic. Textile, food processing, pharma, automotive, electronics and other sectors are data packs, not code-level product boundaries.
