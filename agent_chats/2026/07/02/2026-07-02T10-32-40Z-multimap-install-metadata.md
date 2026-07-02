# Multi-Map Install Metadata Parity

## Prompt

- Continue the active FoolFrame migration goal.
- Current slice: persist legacy multi-map DBMaps metadata during app module
  source installation.

## Scope

- Added `AppInstalledMultiDbMap` for legacy `SW_SYS_MULTIMAP`.
- `DaoAppInstallGateway` now creates missing DBMap rows for multi-map
  properties whether the owning property is newly created or already exists.
- Added `SW_SYS_MULTIMAP` to the Docker MySQL model schema.
- Updated `docs/migration/foolframe-parity.md`.

## Changed Files

- `fool-app-manage/src/main/java/org/fool/framework/app/AppInstalledMultiDbMap.java`
- `fool-app-manage/src/main/java/org/fool/framework/app/DaoAppInstallGateway.java`
- `fool-app-manage/src/test/java/org/fool/framework/app/AppManageMigrationTest.java`
- `docker/mysql/init/005-model.sql`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/02/2026-07-02T10-32-40Z-multimap-install-metadata.md`

## Validation

- Red:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-app-manage -am -DfailIfNoTests=false -Dtest=AppManageMigrationTest test`
  failed at test compile because `AppInstalledMultiDbMap` did not exist.
- Green focused:
  the same command passed with `BUILD SUCCESS`; `AppManageMigrationTest` ran
  36 tests with 0 failures and 0 errors.
- Full backend:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false test`
  passed with `BUILD SUCCESS` across the 15-module reactor.
- Repository harness:
  `python scripts/check_repo_harness.py` passed.
- Whitespace:
  `git diff --check` passed with no output.
- Runtime stack:
  `docker compose ps` showed backend and frontend up, with MySQL and Redis
  healthy.
- Schema:
  `docker compose exec -T mysql mysql -uroot -pPa88word car_wash < docker/mysql/init/005-model.sql`
  completed, and `SHOW COLUMNS FROM SW_SYS_MULTIMAP` showed `SysId`,
  `MAP_NAME`, `MAP_COLNAME`, and `SW_SYS_PROPERTY_DBMapsSysId`.

## Risks

- This slice persists DBMaps metadata. Deeper DBMaps query/runtime behavior
  outside same-row loading, list-query aliasing, and install metadata remains
  open.
