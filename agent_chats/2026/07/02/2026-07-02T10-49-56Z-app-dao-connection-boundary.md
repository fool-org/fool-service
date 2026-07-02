# App DAO Connection Boundary

## Prompt

- Continue the active FoolFrame migration goal.
- Current slice: tighten routed app-management database connection behavior.

## Scope

- `DriverManagerAppDaoServiceFactory` now gives each cached legacy SqlCon route
  one suppress-close JDBC connection instead of using a new driver-manager
  connection per operation.
- Added a focused migration test for the routed `SingleConnectionDataSource`
  boundary.
- Updated `docs/migration/foolframe-parity.md`.

## Changed Files

- `fool-app-manage/src/main/java/org/fool/framework/app/DriverManagerAppDaoServiceFactory.java`
- `fool-app-manage/src/test/java/org/fool/framework/app/AppManageMigrationTest.java`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/02/2026-07-02T10-49-56Z-app-dao-connection-boundary.md`

## Validation

- Focused AppManage:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-app-manage -am -DfailIfNoTests=false -Dtest=AppManageMigrationTest test`
  passed with 37 tests, 0 failures, and 0 errors.
- Full backend:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false test`
  passed across all 15 Maven modules.
- Repo harness:
  `python scripts/check_repo_harness.py`
  passed.
- Whitespace:
  `git diff --check`
  passed.
- Docker runtime:
  `docker compose ps`
  showed backend/frontend up and MySQL/Redis healthy.

## Risks

- This is a minimal routed connection boundary. It is not a full connection
  pool; switch this route to Hikari if concurrent app installs need pooling.
- Routed `DaoService` instances are still manually constructed, so broader
  Spring transaction boundaries remain open.
