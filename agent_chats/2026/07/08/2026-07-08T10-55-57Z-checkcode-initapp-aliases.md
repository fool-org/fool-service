# Check-code and initapp legacy aliases

## Prompt

Continue the FoolFrame migration, keep Vue as the frontend, control file size,
reuse code, and keep rendered pages/data loading View-first instead of binding
to concrete business DTOs.

## Scope

- Compared the legacy FoolFrame auth first-hop response shape for
  `getcheckcode` and `initapp`.
- Exposed Pascal aliases for check-code, initapp, and store database response
  fields while retaining the existing camel-case compatibility fields.
- Extended the focused auth controller serialization test coverage for those
  aliases.
- Updated the Vue protocol types to accept the legacy aliases without adding
  business-specific DTO bindings.
- Tightened `scripts/runtime_doctor.py` so Docker auth smoke requires
  `initapp.Dbs`, `initapp.CheckCode.Key`, and `getcheckcode.Key/Code`.
- Updated `tasks.md` and `docs/migration/foolframe-parity.md`.

## Changed Files

- `fool-auth/src/main/java/org/fool/framework/auth/api/LoginController.java`
- `fool-auth/src/main/java/org/fool/framework/auth/business/service/AuthService.java`
- `fool-auth/src/main/java/org/fool/framework/auth/business/service/CheckCodeService.java`
- `fool-auth/src/test/java/org/fool/framework/auth/api/LoginControllerLogoutTest.java`
- `frontend/src/api.ts`
- `scripts/runtime_doctor.py`
- `tasks.md`
- `docs/migration/foolframe-parity.md`

## Validation

- `docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-auth -am -DfailIfNoTests=false -Dtest=LoginControllerLogoutTest test`: passed, 10 tests.
- `cd frontend && npm test`: passed, 68 tests.
- `python scripts/runtime_doctor_test.py`: passed, 17 tests.
- `cd frontend && npm run build`: passed.
- `docker compose up -d --build backend`: backend image built successfully.
- `docker compose up -d --force-recreate backend`: backend container recreated
  from the rebuilt image.
- `python scripts/runtime_doctor.py`: passed all compose, auth, View/data,
  detail, `inputquery`, report, message, notify, and logout checks.
- `python scripts/check_repo_harness.py`: passed.
- `git diff --check`: clean.

## Runtime Evidence

- Backend: `http://localhost:8080/test` returned the expected smoke list.
- Frontend proxy: `http://localhost:8081` handled the runtime doctor auth and
  View/data API checks.
- Docker services after validation: backend, frontend, MySQL, and Redis were
  running; MySQL and Redis were healthy.

## Risks

- These aliases intentionally duplicate response fields for FoolFrame
  compatibility. A later contract cleanup should be based on an explicit
  compatibility decision, not on a per-page business DTO shortcut.
