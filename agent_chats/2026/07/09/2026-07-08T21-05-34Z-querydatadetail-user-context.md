# QueryDataDetail User Context

## Prompt

Continue the Docker/Vue FoolFrame migration, keep View/data lookup first, and
avoid concrete business DTO binding.

## Scope

- Added a shared `LegacyContextValueService` boundary in `fool-common`.
- Let `OperationCommandValueResolver` resolve `@...` context expressions
  through an optional callback while preserving the existing no-context call
  path for operation and trigger callers.
- Added `fool-auth` token-backed `@userid` / `@username` context values.
- Wired `querydatadetail.IdExp` to resolve user context from the request token
  before loading the detail object through the rendered View model.
- Strengthened the detail service test so `getViewData` must happen before
  View-model data loading.
- Left app/database connection context expressions out of scope; those need
  real app/session connection state, not a hard-coded fallback.

## Changed Files

- `fool-common/src/main/java/org/fool/framework/common/context/LegacyContextValueService.java`
- `fool-auth/src/main/java/org/fool/framework/auth/business/service/LegacyAuthContextValueService.java`
- `fool-auth/src/test/java/org/fool/framework/auth/business/service/LegacyAuthContextValueServiceTest.java`
- `fool-model/src/main/java/org/fool/framework/model/service/OperationCommandValueResolver.java`
- `fool-model/src/test/java/org/fool/framework/model/service/OperationCommandValueResolverTest.java`
- `fool-view/src/main/java/org/fool/framework/view/service/DataQueryService.java`
- `fool-view/src/test/java/org/fool/framework/view/service/DataQueryServiceDetailTest.java`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/09/2026-07-08T21-05-34Z-querydatadetail-user-context.md`

## Validation

- RED: `docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-model -am -Dtest=OperationCommandValueResolverTest test`
  initially failed because `OperationCommandValueResolver.resolve(...)` had no
  context callback overload.
- GREEN: same command passed after adding the callback overload.
- RED: `docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -Dtest=DataQueryServiceDetailTest#queryLegacyViewDataDetailResolvesContextIdExpressionFromToken test`
  initially failed because `LegacyContextValueService` did not exist.
- GREEN: same command passed after wiring the shared context service.
- GREEN: `docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-auth -am -Dtest=LegacyAuthContextValueServiceTest test`
- GREEN: `docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -Dtest=DataQueryServiceDetailTest test`
- GREEN: `docker compose build backend >/tmp/fool_backend_build.log 2>&1 && docker compose up -d backend frontend >/tmp/fool_backend_up.log 2>&1`
- GREEN: `python scripts/runtime_doctor.py`
- GREEN: `python scripts/check_repo_harness.py`
- GREEN: `git diff --check`

## Risks

- Only `@userid` and `@username` are backed by auth context in this slice.
  FoolFrame app/database connection expressions such as `@appcon` and
  `@datacon` remain open until app-session connection state is migrated.
