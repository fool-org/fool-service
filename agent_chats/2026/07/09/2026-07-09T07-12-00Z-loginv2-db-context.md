# LoginV2 Database Context

## Prompt

Continue the Docker/Vue FoolFrame migration with atomic commits and maximum
reuse.

## Scope

- Rechecked FoolFrame `GetValueExpression`: `@appcon` reads `context.AppCon`,
  and `@datacon` reads `context.CurrentCon`.
- Stored the selected legacy `DbId` beside the runtime token, reusing the
  existing Redis token-session pattern.
- Resolved token-backed `@appcon` / `@datacon` through existing
  `SW_APPLICATION.SW_APP_CON` and token-selected `SW_STOREDB.SW_STORE_CON`
  rows.
- Kept broader `CacheStore` fields, `@syscon`, `@modelcon`, and `@context`
  out of this slice until a migrated consumer needs them.

## Changed Files

- `fool-auth/src/main/java/org/fool/framework/auth/business/common/RedisKeyPrefix.java`
- `fool-auth/src/main/java/org/fool/framework/auth/business/service/TokenService.java`
- `fool-auth/src/main/java/org/fool/framework/auth/business/service/AuthService.java`
- `fool-auth/src/main/java/org/fool/framework/auth/business/service/LegacyAuthContextValueService.java`
- `fool-auth/src/main/java/org/fool/framework/auth/api/LoginController.java`
- `fool-auth/src/test/java/org/fool/framework/auth/business/service/AuthServiceLegacyMenuTest.java`
- `fool-auth/src/test/java/org/fool/framework/auth/business/service/LegacyAuthContextValueServiceTest.java`
- `fool-auth/src/test/java/org/fool/framework/auth/business/service/TokenServiceLogoutTest.java`
- `fool-auth/src/test/java/org/fool/framework/auth/api/LoginControllerLogoutTest.java`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/09/2026-07-09T07-12-00Z-loginv2-db-context.md`

## Validation

- RED: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-auth -am -Dtest=AuthServiceLegacyMenuTest#getLegacyConnectionContextUsesLoginV2SessionAppAndDatabase,LegacyAuthContextValueServiceTest#resolvesLegacyConnectionContextValuesFromToken,LoginControllerLogoutTest#loginV2ReturnsLegacyTokenUserAndApp,TokenServiceLogoutTest#logoutTokenDeletesTokenAndUserTokenKeys test`
  failed with missing `getLegacyDbId`, `getLegacyAppConnection`,
  `getLegacyDataConnection`, `LEGACY_DB_TOKEN_PREFIX`, and the three-argument
  `rememberLegacyApp`.
- GREEN: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-auth -am -Dtest=AuthServiceLegacyMenuTest#getLegacyConnectionContextUsesLoginV2SessionAppAndDatabase,LegacyAuthContextValueServiceTest#resolvesLegacyConnectionContextValuesFromToken,LoginControllerLogoutTest#loginV2ReturnsLegacyTokenUserAndApp,TokenServiceLogoutTest#logoutTokenDeletesTokenAndUserTokenKeys test`
- GREEN: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-auth -am test`
- GREEN: `python scripts/check_repo_harness.py`
- GREEN: `git diff --check`
- GREEN: `docker compose build backend`
- GREEN: `docker compose up -d backend frontend`
- GREEN: `python scripts/runtime_doctor.py`

## Risks

- `@syscon`, `@modelcon`, `@context`, and broader `CacheStore` state are still
  not migrated.
