# InputQuery Owner Context

## Prompt

- Continue the FoolFrame migration with Docker runtime, Vue frontend, and timely atomic commits.

## Scope

- Migrated the legacy `inputquery` added-item owner-context source-list branch for `IsAdded + OwnerId`.
- Added runtime `Model.owner` metadata for legacy `MODEL_DEFAULTOWNER`.
- Kept broader FoolFrame expression evaluation out of scope; this slice supports owner source expressions such as `#.availableCustomers`.

## Changes

- `Model.owner` now maps to `fool_sys_model.default_owner`.
- `AppInstalledModel.defaultOwnerId` now maps to legacy `SW_SYS_MODEL.MODEL_DEFAULTOWNER`.
- Docker MySQL init DDL creates/backfills both default-owner columns and indexes.
- `DataQueryService.inputQuery` now loads the parent model object when `isAdded`, `ownerId`, `Model.owner`, and a `#.` source expression are present, then filters the parent source collection with the existing case-insensitive source-list logic.
- The running Docker MySQL volume was patched with `fool_sys_model.default_owner` and `SW_SYS_MODEL.MODEL_DEFAULTOWNER`.
- Migration parity docs now mark the added-item owner source-list branch as migrated and narrow the remaining gap to richer expression evaluation.

## Validation

- RED: `docker run --rm -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false -Dtest=DataQueryServiceInputQueryTest,ModelDaoMappingTest test`
  - Failed before implementation with `cannot find symbol: method setOwner(...)` on `Model`, proving the owner metadata surface was absent.
- GREEN: same focused `fool-view` command passed after implementation with 8 tests.
- AppInstall regression: `docker run --rm -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-app-manage -am -DfailIfNoTests=false -Dtest=AppManageMigrationTest test`
  - Passed with 40 tests.
- Full backend regression: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false test`
  - Passed with `BUILD SUCCESS` across the full reactor.
- Repo harness: `python scripts/check_repo_harness.py`
  - Passed with `Repository harness validation passed.`
- Whitespace: `git diff --check`
  - Passed with no output.
- Runtime DB patch: `docker compose exec -T mysql mysql -uroot -pPa88word car_wash -e "SHOW COLUMNS FROM fool_sys_model LIKE 'default_owner'; SHOW COLUMNS FROM SW_SYS_MODEL LIKE 'MODEL_DEFAULTOWNER';"`
  - Passed and showed both columns; `fool_sys_model.default_owner` was indexed.
- Docker runtime: `docker compose up -d --build backend`
  - Rebuilt the backend image and restarted `fool-service-backend-1`.
- HTTP smoke: `curl --retry 20 --retry-delay 2 --retry-all-errors -fsS http://localhost:8080/test`
  - Passed and returned seeded order rows.
- HTTP smoke: `curl --retry 20 --retry-delay 2 --retry-all-errors -fsS -H 'Content-Type: application/json' -d '{"viewName":"OrderList","viewItemId":"symbol","text":"BTC"}' http://localhost:8080/api/v1/data/inputquery`
  - Passed with `{"code":0,"message":"success","data":{"items":[]}}`.
- Runtime status: `docker compose ps`
  - Backend, frontend, MySQL, and Redis were up; MySQL and Redis were healthy.

## Skipped Checks

- Frontend tests were not rerun because this change did not touch `frontend/`.

## Risks

- The Java branch supports the legacy owner-source shape used by `#.propertyName`; it does not implement the full FoolFrame string expression engine.
- Existing app-install flows only preserve default owner IDs that are already present on source `Model` metadata.

## Follow-ups

- Continue migrating richer `inputquery` expression evaluation and saved-query/report integration.
