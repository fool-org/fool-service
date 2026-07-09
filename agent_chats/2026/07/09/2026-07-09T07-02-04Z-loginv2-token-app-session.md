# LoginV2 Token App Session

## Prompt

Continue the Docker/Vue FoolFrame migration, keep commits atomic, maximize
reuse, and avoid speculative protocol work.

## Scope

- Rechecked the legacy auth/session path around `loginv2`, `getapp`, and
  `getmain`.
- Added focused tests proving a selected App id from `loginv2` is remembered
  by token and later used by `getLegacyAppInfo(token)`.
- Added logout cleanup for the token-selected legacy App key.
- Kept the slice to AppInfo selection only. Selected database connection
  context for `@datacon` / `@appcon` still needs a real consumer and is not
  faked here.

## Changed Files

- `fool-auth/src/main/java/org/fool/framework/auth/business/common/RedisKeyPrefix.java`
- `fool-auth/src/main/java/org/fool/framework/auth/business/service/TokenService.java`
- `fool-auth/src/main/java/org/fool/framework/auth/business/service/AuthService.java`
- `fool-auth/src/main/java/org/fool/framework/auth/api/LoginController.java`
- `fool-auth/src/test/java/org/fool/framework/auth/business/service/AuthServiceLegacyMenuTest.java`
- `fool-auth/src/test/java/org/fool/framework/auth/business/service/TokenServiceLogoutTest.java`
- `fool-auth/src/test/java/org/fool/framework/auth/api/LoginControllerLogoutTest.java`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/09/2026-07-09T07-02-04Z-loginv2-token-app-session.md`

## Validation

- RED: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-auth -am -Dtest=AuthServiceLegacyMenuTest#getLegacyAppInfoUsesLoginV2SessionApplication,LoginControllerLogoutTest#loginV2ReturnsLegacyTokenUserAndApp test`
  failed with missing `TokenService.getLegacyAppId` and
  `AuthService.rememberLegacyApp`.
- GREEN: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-auth -am -Dtest=AuthServiceLegacyMenuTest#getLegacyAppInfoUsesLoginV2SessionApplication,LoginControllerLogoutTest#loginV2ReturnsLegacyTokenUserAndApp,TokenServiceLogoutTest#logoutTokenDeletesTokenAndUserTokenKeys test`
- GREEN: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-auth -am test`
- GREEN: `python scripts/check_repo_harness.py`
- GREEN: `git diff --check`
- GREEN: `docker compose build backend`
- GREEN: `docker compose up -d backend frontend`
- GREEN: `python scripts/runtime_doctor.py`

## Risks

- The token now remembers the selected App id only. The selected `DbId` still
  does not drive app/database connection context values.
