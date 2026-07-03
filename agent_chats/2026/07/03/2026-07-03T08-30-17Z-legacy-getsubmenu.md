# Prompt

Continue the FoolFrame migration with Docker runtime, Vue frontend, and timely
atomic commits.

# Scope

- Compare the legacy `Soway.Server` `getsubmenu` contract.
- Expose top-level and child legacy authorized-menu lookup in Spring.
- Surface the flow in the Vue operator console.

# Changes

- Added `POST /api/v1/auth/getsubmenu`.
- Accepted legacy `Token` and `ParentAuthCode` request fields.
- Resolved the current authorized user through the token service.
- Queried Docker-seeded legacy auth/menu role tables for top-level and child
  menus.
- Returned legacy `Items` / `Token` with `AuthItem` fields including `AuthNo`,
  `Text`, `Note`, `ImageUrl`, `AuthType`, `ViewId`, `NotifyCount`,
  `ViewType`, and `Index`.
- Added Vue API types and a Sub Menu panel that calls the legacy route and
  renders returned menu rows.
- Updated `docs/migration/foolframe-parity.md`.

# Changed Files

- `fool-auth/src/main/java/org/fool/framework/auth/api/LoginController.java`
- `fool-auth/src/main/java/org/fool/framework/auth/business/service/AuthService.java`
- `fool-auth/src/test/java/org/fool/framework/auth/api/LoginControllerLogoutTest.java`
- `fool-auth/src/test/java/org/fool/framework/auth/business/service/AuthServiceLegacyMenuTest.java`
- `frontend/src/App.vue`
- `frontend/src/api.ts`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`

# Validation

- RED: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-auth -am -DfailIfNoTests=false -Dtest=AuthServiceLegacyMenuTest,LoginControllerLogoutTest test`
  failed because the legacy submenu request/result types and service/controller
  methods did not exist.
- PASS: same focused Maven command after implementation.
  - Passed: 8 tests, 0 failures, 0 errors.
- RED: `cd frontend && npm test -- --run payload.test.ts`
  failed because the Vue console did not expose Sub Menu.
- PASS: `cd frontend && npm test -- --run payload.test.ts`
  - Passed: 27 tests.
- PASS: `cd frontend && npm test`
  - Passed: 27 tests.
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
- PASS: `POST http://localhost:8080/api/v1/auth/getsubmenu`
  - Legacy `Token` payload returned code `0`, the same token, and top item
    `authNo=1`, `text=Views`, `viewId=0`.
- PASS: `POST http://localhost:8080/api/v1/auth/getsubmenu`
  - Legacy `Token` / `ParentAuthCode=1` payload returned child item
    `authNo=2`, `text=OrderList`, `viewId=100`.
- PASS: same child submenu request through
  `http://localhost:8081/api/v1/auth/getsubmenu`.
- PASS: `GET http://localhost:8081/` returned HTTP `200`.
- PASS: `docker compose ps`
  - Backend and frontend up; MySQL and Redis healthy.

# Risks

- The migrated menu response covers the observed legacy top/child menu
  AuthItem shape. Broader application-entry routes such as `getapp` and
  `getmain` remain separate migration work.
- Notify counts remain `0` in submenu items until a concrete legacy count
  source is identified.

# Follow-ups

- Continue legacy app/menu shell routes such as `getapp`, `getmain`, or
  operation execution.
