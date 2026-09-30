# Anumati Demo Runbook

> Start with `docs/PROJECT-WALKTHROUGH.md` when you need the full route map and navigation model.

## Scenario
Use the seeded `Reference Manufacturing Unit` business. It explicitly declares `environmentalConsentRequired=true`, so the prototype can demonstrate a source-backed consent path without inferring legal applicability from the sector label.

## Applicant journey
1. Sign in as the configured applicant user.
2. Open `Reference Manufacturing Unit`.
3. Run Regulatory Analysis.
4. Show the `MPCB-CTE` result, its source link, rule ID and the scrutiny tier.
5. Open Evidence Passport and Pre-Flight.
6. Show the document requirements derived from the verified MPCB source.
7. Open Regulatory Impact and switch the business stage from Setup to Operating in the simulation. The simulator should show the regulatory delta without mutating the saved profile.
8. Return to the saved profile and demonstrate version-aware history.

## Sector-neutral second scenario
1. Open the seeded `Reference Food Processing Unit` in Nashik.
2. Run Regulatory Analysis.
3. Show the FSSAI licence/registration result driven by the explicit `foodBusinessOperator=true` attribute.
4. Explain that the same rule engine, evidence model and lifecycle are reused for a different sector and geography.

## Officer journey
1. Sign in as the configured department officer.
2. Open the Control Tower.
3. Review application lanes, dependency blockers and SLA state.
4. Open an application and demonstrate scrutiny, query, response, inspection and decision workflows when test data is present.

## Admin journey
1. Sign in as the configured admin user.
2. Open Regulatory Catalogue.
3. Show the verified MPCB sources.
4. Show the source-backed approvals, rules, document requirements and SLA configuration.
5. Explain that new regulatory material is admitted through the verification boundary before it can drive decisions.

## Trust points to demonstrate
- Rule evaluation is deterministic.
- Only verified sources drive regulatory decisions.
- Business state and analysis runs are versioned.
- Evidence is reusable but tied to the profile version it was verified against.
- Pre-Flight blocks avoidable submission errors.
- Statutory decisions remain with authorised departments.

## Do not claim
- A guaranteed reduction in statutory approval time.
- Complete Maharashtra coverage.
- Legal certification of uploaded documents.
- Autonomous government decision-making.
- Risk scores as legal determinations.
> Local document storage uses the `local` Spring profile so the demo does not require an S3 account.
