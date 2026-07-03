# Prompt

Continue the FoolFrame migration with Docker runtime, Vue frontend, and timely
atomic commits.

# Scope

- Compare the legacy `Soway.Server` `getmain` contract and old Node web call.
- Expose the raw-token legacy main-info shell in Spring.
- Surface the flow in the Vue operator console.

# Changes

- Added `POST /api/v1/auth/getmain`.
- Accepted the legacy raw token request body used by the old Node web client.
- Returned legacy `Token`, `User`, `App`, and `TopMenu` fields.
- Reused the migrated token/user lookup and top-menu lookup paths.
- Added Vue API types plus a Main Info button in the auth panel.
- Updated `docs/migration/foolframe-parity.md`.

# Changed Files

- `fool-auth/src/main/java/org/fool/framework/auth/api/LoginController.java`
- `fool-auth/src/test/java/org/fool/framework/auth/api/LoginControllerLogoutTest.java`
- `frontend/src/App.vue`
- `frontend/src/api.ts`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`

# Validation

- RED: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-auth -am -DfailIfNoTests=false -Dtest=LoginControllerLogoutTest test`
  failed because `LoginController.LegacyMainResult` and `getMain(String)` did
  not exist.
- PASS: same focused Maven command after implementation.
  - Passed: 7 tests, 0 failures, 0 errors.
- RED: `cd frontend && npm test -- --run payload.test.ts`
  failed because the Vue console did not expose Main Info.
- PASS: `cd frontend && npm test -- --run payload.test.ts`
  - Passed: 28 tests.
- PASS: `cd frontend && npm test`
  - Passed: 28 tests.
- PASS: `cd frontend && npm run build`
- PASS: `python scripts/check_repo_harness.py`
- PASS: `git diff --check`

# Runtime Evidence

- PASS: `docker compose up -d --build`
  - Backend Maven package and frontend production build passed.
- PASS: `GET http://localhost:8080/test`
  - Returned HTTP `200` before route smoke.
- PASS: `POST http://localhost:8080/api/v1/auth/login`
  - Seeded `admin` user returned a token.
- PASS: `POST http://localhost:8080/api/v1/auth/getmain`
  - Raw JSON token body returned code `0`, the same token, user
    `loginName=admin`, top menu `authNo=1`, `text=Views`, `viewId=0`, and an
    empty `App` shell.
- PASS: same raw-token `getmain` request through
  `http://localhost:8081/api/v1/auth/getmain`.
- PASS: `GET http://localhost:8081/` returned HTTP `200`.
- PASS: `docker compose ps`
  - Backend and frontend up; MySQL and Redis healthy.

# Risks

- `App` is intentionally an empty legacy shell because the current migrated
  Spring login does not yet store legacy app context. Wire this to migrated
  `loginv2` / app session state when that route lands.
- `getmain` currently covers the old web flow's user and top-menu needs. Full
  app metadata remains `getapp` / `loginv2` migration work.

# Follow-ups

- Continue legacy app shell routes such as `getapp` or `loginv2` app context.
