# Legacy Exoperation Alias

## Prompt

- Continue the Docker/Vue FoolFrame migration.
- Keep the migration View-first and reuse existing protocol/service paths.

## Scope

- Compared FoolFrame Web `public/javascripts/app/operation.js`.
- Added backend compatibility for the old Web operation route shape:
  `/api/v1/data/exoperation` with `objid`, `viewid`, and `opid`.
- Reused the existing `runoperation` controller/service path; no new operation
  execution logic was added.

## Changed Files

- `fool-view/src/main/java/org/fool/framework/view/api/DataController.java`
- `fool-view/src/main/java/org/fool/framework/view/dto/LegacyRunOperationRequest.java`
- `fool-view/src/test/java/org/fool/framework/view/api/DataControllerRunOperationTest.java`
- `scripts/runtime_doctor.py`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/10/2026-07-09T16-25-46Z-exoperation-alias.md`

## Validation

- `mvn -pl fool-view -Dtest=DataControllerRunOperationTest test`
  - Skipped as invalid local evidence: the local Maven run could not resolve
    reactor artifacts without `-am`.
- `mvn -pl fool-view -am -Dtest=DataControllerRunOperationTest test`
  - Skipped as invalid local evidence: local Java is not Java 17
    (`invalid target release: 17`).
- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -Dtest=DataControllerRunOperationTest test`
  - Passed: 4 tests, 0 failures.
- `python scripts/runtime_doctor_test.py`
  - Passed: 35 tests.
- `docker compose up -d --build backend`
  - Backend image rebuilt; Maven `-DskipTests package` reactor build passed.
- `python scripts/runtime_doctor.py`
  - Passed, including `data:exoperation-legacy-aliases`.

## Runtime Artifacts

- Backend: `http://localhost:8080`
- Frontend proxy: `http://localhost:8081`
- Docker services: backend, frontend, MySQL, and Redis are running.

## Skipped Checks

- No frontend tests were run; this slice did not change frontend code.

## Risks And Follow-Ups

- This preserves the legacy Web operation alias only under the migrated
  `/api/v1/data` prefix. A raw `/data/exoperation` compatibility route was not
  added because the current Vue/Nginx runtime routes API calls through
  `/api/*`.
