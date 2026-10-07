# API Change Radar

API Change Radar watches third-party OpenAPI/Swagger specs, detects breaking/non-breaking/deprecation changes, and alerts users via Email/Slack/Custom webhook.

## Tech Stack
- Java 17, Spring Boot 3.3.5, Maven
- Spring Web, WebFlux, Data JPA, Security(JWT), Validation, Mail, Scheduling, Async
- PostgreSQL + Flyway
- swagger-parser, Resilience4j (retry+circuit breaker)
- Actuator + Micrometer
- springdoc-openapi
- JUnit5, Mockito, Testcontainers
- Docker Compose

## Quick Start (Local)

1) Start infra
```bash
docker compose up -d postgres mailhog
```
- Postgres: localhost:5432
- MailHog UI: http://localhost:8025

2) Build & run
```bash
mvn clean compile
mvn spring-boot:run
```

3) Health
```bash
curl http://localhost:8080/actuator/health
```

## Auth
Register:
```bash
curl -X POST http://localhost:8080/auth/register -H "Content-Type: application/json" -d "{\"name\":\"A\",\"email\":\"a@b.com\",\"password\":\"password123\"}"
```
Login:
```bash
curl -X POST http://localhost:8080/auth/login -H "Content-Type: application/json" -d "{\"email\":\"a@b.com\",\"password\":\"password123\"}"
```
Use token as `Authorization: Bearer <token>`

## Monitor an API
```bash
curl -X POST http://localhost:8080/api/monitored-apis -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -d "{\"name\":\"Payments\",\"specUrl\":\"https://example.com/payment-api-v1.json\",\"checkIntervalSeconds\":60,\"active\":true}"
```

## Alert Channels
```bash
curl -X POST http://localhost:8080/api/alert-channels -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -d "{\"type\":\"EMAIL\",\"target\":\"you@example.com\",\"enabled\":true}"
```

## Change History & Diff
List reports:
```bash
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/monitored-apis/{apiId}/changes
```
Diff versions:
```bash
curl -X POST http://localhost:8080/api/monitored-apis/{apiId}/changes/diff -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -d "{\"fromVersion\":1,\"toVersion\":2}"
```

## API Docs
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

## Tests
```bash
mvn test
```

## Sample Specs
See `src/test/resources/openapi/payment-api-v1.json` and `payment-api-v2.json`

