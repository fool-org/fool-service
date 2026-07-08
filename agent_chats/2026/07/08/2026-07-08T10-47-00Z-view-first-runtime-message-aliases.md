# View-first runtime doctor and message aliases

## Prompt

Continue the FoolFrame migration, keep the frontend Vue-based, and avoid
binding rendered pages or validation to concrete business DTOs.

## Scope

- Tightened `scripts/runtime_doctor.py` so `querydata`, `inputquery`, and
  report checks derive their request shape from the loaded View metadata.
- Added runtime-doctor helper tests that use neutral record/status examples
  instead of Docker order fields.
- Exposed FoolFrame Pascal aliases for legacy `getmsg` / `getnotify`
  responses while retaining existing camel-case fields for the Vue panel.
- Updated `tasks.md` and `docs/migration/foolframe-parity.md`.

## Changed Files

- `business-application/src/main/java/org/fool/framework/application/api/MessageController.java`
- `business-application/src/test/java/org/fool/framework/application/api/MessageControllerTest.java`
- `frontend/src/api.ts`
- `scripts/runtime_doctor.py`
- `scripts/runtime_doctor_test.py`
- `tasks.md`
- `docs/migration/foolframe-parity.md`

## Validation

- `python scripts/runtime_doctor_test.py`: passed, 17 tests.
- `docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl business-application -am -DfailIfNoTests=false -Dtest=MessageControllerTest test`: passed, 2 tests.
- `cd frontend && npm test`: passed, 68 tests.
- `cd frontend && npm run build`: passed.
- `docker compose up -d --build backend frontend`: images built successfully.
- `python scripts/runtime_doctor.py`: first run proved all View-first checks
  passed but message aliases failed because the backend container had not been
  recreated.
- `docker compose up -d --force-recreate backend`: backend recreated.
- `python scripts/runtime_doctor.py`: passed all compose, auth shell,
  View/data/detail, `inputquery`, report, message, notify, and logout checks.

## Runtime Evidence

- Backend: `http://localhost:8080/test` returned the expected smoke list.
- Frontend proxy: `http://localhost:8081` handled all runtime doctor API
  checks.
- Docker services after validation: backend, frontend, MySQL, and Redis were
  running; MySQL and Redis were healthy.

## Risks

- `querydata` still emits a compatibility `values` map in addition to legacy
  `Items`; Vue rendering and runtime validation now use `Items`, but removing
  `values` would be a separate compatibility decision.
