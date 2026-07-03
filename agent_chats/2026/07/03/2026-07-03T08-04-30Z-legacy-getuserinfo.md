# Prompt

Continue the FoolFrame migration with Docker runtime, Vue frontend, and timely
atomic commits.

# Scope

- Compare the legacy `Soway.Server/User/HandlerUserInfo.cs` handler.
- Expose a minimal token-to-user legacy wrapper using the existing auth token
  service.

# Changes

- Added `POST /api/v1/auth/getuserinfo`.
- Added legacy `Token` alias support to `CommonRequest`.
- Returned legacy `Token` plus `User.loginName`, `User.userName`,
  `User.userId`, and empty company/department/avatar fields.
- Added Vue API type support and a Legacy User Info button in the auth panel.
- Updated `docs/migration/foolframe-parity.md`.

# Validation

- RED: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-auth -am -DfailIfNoTests=false -Dtest=LoginControllerLogoutTest test`
  failed because `LoginController.LegacyUserInfoResult` and
  `LoginController.getUserInfo` did not exist.
- PASS: same focused Maven command after implementation.
  - Passed: 2 tests, 0 failures, 0 errors.
- RED: `npm test -- --run payload.test.ts`
  failed because the Vue console did not expose Legacy User Info.
- PASS: `npm test -- --run payload.test.ts`
  - Passed: 25 tests.

# Runtime Evidence

- PASS: `docker compose up -d --build`
  - Backend Maven package and frontend production build passed.
- Login with `admin` / `admin` returned a token.
- `POST http://localhost:8080/api/v1/auth/getuserinfo` with legacy `Token`
  returned code `0`, token echo, `user.loginName=admin`, `user.userName=Admin`,
  and `user.userId=0`.
- `POST http://localhost:8081/api/v1/auth/getuserinfo` returned the same user
  payload through the Vue frontend proxy.
- `GET http://localhost:8080/test` returned HTTP `200`.
- `GET http://localhost:8081/` returned HTTP `200`.

# Risks

- The public FoolFrame `DataService.GetUserInfo` method throws
  `NotImplementedException`; this migration follows the adjacent
  `HandlerUserInfo` payload shape.
- Company, department, and avatar fields are empty until a real legacy source is
  identified in the migrated auth graph.

# Follow-ups

- Continue legacy `getapp`, `getmain`, submenu, or operation execution after
  this route is runtime-smoked.
