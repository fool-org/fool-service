# Legacy View File Mapping

## Prompt

Continue the Docker/FoolFrame/Vue migration goal with timely atomic commits.

## Scope

- Added the app-install Java mapping for legacy `ViewTemplateFile` /
  `SW_SYS_VIEW_FILE`.
- Updated the migration parity document and mapping test for the new mapping.

## Changed Files

- `fool-app-manage/src/main/java/org/fool/framework/app/AppInstalledViewFile.java`
- `fool-app-manage/src/test/java/org/fool/framework/app/AppManageMigrationTest.java`
- `docs/migration/foolframe-parity.md`

## Validation

- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-app-manage -am -Dtest=AppManageMigrationTest#mapsApplicationAndStoreDatabaseToLegacyTables test`
- `docker compose exec -T mysql mysql -uroot -pPa88word car_wash -e "SHOW COLUMNS FROM SW_SYS_VIEW_FILE;"`
- `python scripts/check_repo_harness.py`
- `curl -sS -m 5 -H 'Content-Type: application/json' -d '{"viewName":"OrderList"}' http://localhost:8080/api/v1/view/get-view`
- `docker compose ps`

## Skipped Checks

- No full Maven reactor run; this was a one-class mapping slice covered by the
  app-manage mapping test and runtime smoke.

## Risks And Follow-Ups

- This maps the legacy table shape. It does not add arbitrary template file
  upload/edit runtime behavior.
