# Prompt

Continue the FoolFrame migration with Docker runtime, Vue frontend, and timely
atomic commits.

# Scope

- Compare the legacy `Soway.Server` `getcheckcode` and `checkcode` contract.
- Expose the legacy check-code generation and validation flow in Spring.
- Surface the flow in the Vue operator console.

# Changes

- Added `POST /api/v1/auth/getcheckcode`.
- Added `POST /api/v1/auth/checkcode`.
- Added Redis-backed check-code storage with the legacy 60-second validation
  window.
- Generated a 4-character check code and base64 JPEG payload.
- Accepted legacy `Key`, `Code`, and `ChkCodeImg` request field aliases.
- Added Vue API types plus a Check Code panel that loads and validates codes.
- Updated `docs/migration/foolframe-parity.md`.

# Validation

- RED: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-auth -am -DfailIfNoTests=false -Dtest=CheckCodeServiceTest test`
  failed because `CheckCodeService` did not exist.
- PASS: same focused Maven command after service implementation.
  - Passed: 2 tests, 0 failures, 0 errors.
- RED: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-auth -am -DfailIfNoTests=false -Dtest=LoginControllerLogoutTest test`
  failed because `LoginController.getCheckCode` and `LoginController.checkCode`
  did not exist.
- PASS: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-auth -am -DfailIfNoTests=false -Dtest=CheckCodeServiceTest,LoginControllerLogoutTest test`
  - Passed: 7 tests, 0 failures, 0 errors.
- RED: `npm test -- --run payload.test.ts`
  failed because the Vue console did not expose Check Code.
- PASS: `npm test -- --run payload.test.ts`
  - Passed: 26 tests.
- PASS: `cd frontend && npm test`
  - Passed: 26 tests.
- PASS: `cd frontend && npm run build`
- PASS: `python scripts/check_repo_harness.py`
- PASS: `git diff --check`

# Runtime Evidence

- PASS: `docker compose up -d --build`
  - Backend Maven package and frontend production build passed.
- PASS: `POST http://localhost:8080/api/v1/auth/getcheckcode`
  - Returned code `0`, key length `36`, check code, and base64 image length
    greater than 20.
- PASS: `POST http://localhost:8080/api/v1/auth/checkcode`
  - Legacy `Key` / `Code` payload with lower-case whitespace-padded code
    returned `data=true`.
- PASS: `POST http://localhost:8080/api/v1/auth/checkcode`
  - Wrong lower-case payload returned `data=false`.
- PASS: same `getcheckcode` / `checkcode` flow through
  `http://localhost:8081/api/v1/auth/*`.
- PASS: `GET http://localhost:8080/test` returned HTTP `200`.
- PASS: `GET http://localhost:8081/` returned HTTP `200`.
- PASS: `docker compose ps`
  - Backend and frontend up; MySQL and Redis healthy.

# Risks

- Generated images are intentionally simple Java2D JPEGs. This matches the
  legacy surface shape but not every visual noise/styling detail from
  FoolFrame.
- Validation does not consume/delete the Redis key after a successful check,
  matching the simple legacy session comparison behavior observed in
  `HandlerCheckCode`.

# Follow-ups

- Continue legacy app/menu shell routes such as `getapp`, `getmain`, submenu,
  or operation execution.
