# Anumati Backend

Java 21 + Spring Boot 3.5 + PostgreSQL + Flyway modular monolith.

## Local development

Set PostgreSQL and required environment variables, then run:

```bash
mvn spring-boot:run
```

## API documentation

Authenticated administrators can use:
- `/swagger-ui.html`
- `/v3/api-docs`

Swagger UI uses the session cookie and Spring Security CSRF support.

## Operations

- `/actuator/health` aggregate health
- `/livez` liveness
- `/readyz` readiness
- `/actuator/info` authenticated build information
- `/actuator/prometheus` authenticated Prometheus metrics endpoint

## Validation and errors

All controller request bodies use Jakarta Bean Validation where applicable. Cross-field and domain invariants are enforced in application services. Errors use RFC 7807-style `ProblemDetail` responses with an `errorCode` and `requestId`.

## Security

Authentication uses an opaque, server-stored session token in an HttpOnly cookie with CSRF protection. The current local identity provider is intentionally simple; production identity should be OIDC/Cognito.

## Regulatory trust

Only verified sources are eligible for regulatory applicability, SLA configuration, incentive matching and compliance obligations.
