# Legacy Operation Java Mappings

## Prompt

- Continue the Docker/Vue FoolFrame migration and keep commits atomic.

## Scope

- Added app-install Java mappings for legacy operation tables:
  `SW_SYS_OPERATION`, `SW_SYS_COMMANDS`, and `SW_SYS_OPERATION_PARAM`.
- Extended the app-management migration mapping test.
- Updated the migration parity document.

## Changed Files

- `fool-app-manage/src/main/java/org/fool/framework/app/AppInstalledOperation.java`
- `fool-app-manage/src/main/java/org/fool/framework/app/AppInstalledOperationCommand.java`
- `fool-app-manage/src/main/java/org/fool/framework/app/AppInstalledOperationParam.java`
- `fool-app-manage/src/test/java/org/fool/framework/app/AppManageMigrationTest.java`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/02/2026-07-02T11-59-16Z-legacy-operation-java-mappings.md`

## Validation

- `docker run --rm -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-app-manage -am -DfailIfNoTests=false -Dtest=AppManageMigrationTest#mapsApplicationAndStoreDatabaseToLegacyTables test`
  - Passed; one focused test ran.
- `python scripts/check_repo_harness.py`
  - Passed.
- `curl -sS -m 5 -H 'Content-Type: application/json' -d '{"viewName":"OrderList"}' http://localhost:8080/api/v1/view/get-view`
  - Returned `code:0`.
- `curl -sS -m 5 -H 'Content-Type: application/json' -d '{"viewName":"OrderList","pageInfo":{"pageSize":10,"pageIndex":1},"filter":null}' http://localhost:8080/api/v1/data/query-list`
  - Returned `code:0` with two seeded rows.

## Skipped Checks

- Full Maven and frontend suites; this slice only adds app-install mapping
  classes and a focused mapping test.

## Risks

- Operation installation orchestration and runtime operation execution remain
  separate migration work.
