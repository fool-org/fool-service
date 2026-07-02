# Legacy Trigger Java Mappings

## Prompt

- Continue the Docker/Vue FoolFrame migration and keep commits atomic.

## Scope

- Added app-install Java mappings for legacy model trigger tables.
- Added app-install Java mappings for legacy property trigger tables.
- Extended the app-management migration mapping test.
- Updated the migration parity document.

## Changed Files

- `fool-app-manage/src/main/java/org/fool/framework/app/AppInstalledModelTrigger.java`
- `fool-app-manage/src/main/java/org/fool/framework/app/AppInstalledModelTriggerCommand.java`
- `fool-app-manage/src/main/java/org/fool/framework/app/AppInstalledPropertyTrigger.java`
- `fool-app-manage/src/main/java/org/fool/framework/app/AppInstalledPropertyTriggerCommand.java`
- `fool-app-manage/src/test/java/org/fool/framework/app/AppManageMigrationTest.java`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/02/2026-07-02T11-54-07Z-legacy-trigger-java-mappings.md`

## Validation

- `mvn -pl fool-app-manage -Dtest=AppManageMigrationTest#mapsApplicationAndStoreDatabaseToLegacyTables test`
  - Failed locally because the active local JDK does not support target 17.
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

- Trigger installation orchestration and runtime execution remain separate
  migration work.
