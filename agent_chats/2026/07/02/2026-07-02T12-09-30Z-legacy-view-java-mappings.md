# Legacy View Java Mappings

## Prompt

Continue the Docker/FoolFrame/Vue migration goal with timely atomic commits.

## Scope

- Added app-install table mappings for legacy `SW_SYS_VIEW`,
  `SW_SYS_VIEW_ITEM`, `SW_SYS_VIEW_OPERATION`, `SW_SYS_OPERATIONVIEW`, and
  `SW_SYS_OPERATIONVIEW_ITEM`.
- Added missing legacy `SW_SYS_VIEW` columns and view/operation-view collection
  owner columns to the Docker MySQL schema.
- Updated the migration parity document for this slice.

## Changed Files

- `docker/mysql/init/006-view.sql`
- `fool-app-manage/src/main/java/org/fool/framework/app/AppInstalledView.java`
- `fool-app-manage/src/main/java/org/fool/framework/app/AppInstalledViewItem.java`
- `fool-app-manage/src/main/java/org/fool/framework/app/AppInstalledViewOperation.java`
- `fool-app-manage/src/main/java/org/fool/framework/app/AppInstalledOperationView.java`
- `fool-app-manage/src/main/java/org/fool/framework/app/AppInstalledOperationViewItem.java`
- `fool-app-manage/src/test/java/org/fool/framework/app/AppManageMigrationTest.java`
- `docs/migration/foolframe-parity.md`

## Validation

- `docker compose exec -T mysql mysql -uroot -pPa88word car_wash < docker/mysql/init/006-view.sql`
- `docker compose exec -T mysql mysql -uroot -pPa88word car_wash -e "SHOW COLUMNS ..."`
  confirmed `VIEW_MODEL`, `VIEW_FILTER`, `VIEW_DEFAULT`, `VIEW_TYPE`,
  `VIEW_FILE`, `VIEW_CHECKAUTH`, `VIEW_AUTOFRESHINTERVAL`, `VIEW_CANEDIT`,
  `SW_SYS_VIEW_ItemsVIEW_ID`, `SW_SYS_VIEW_OperationsVIEW_ID`, and
  `SW_SYS_OPERATIONVIEW_ParamsSysId`.
- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-app-manage -am -Dtest=AppManageMigrationTest#mapsApplicationAndStoreDatabaseToLegacyTables test`
- `python scripts/check_repo_harness.py`
- `curl -sS -m 5 http://localhost:8080/test`
- `curl -sS -m 5 -H 'Content-Type: application/json' -d '{"viewName":"OrderList"}' http://localhost:8080/api/v1/view/get-view`
- `curl -sS -m 5 -H 'Content-Type: application/json' -d '{"viewName":"OrderList","pageInfo":{"pageSize":10,"pageIndex":1},"filter":null}' http://localhost:8080/api/v1/data/query-list`
- `docker compose ps`

## Skipped Checks

- Local host Maven was not used as the passing gate because the host JDK cannot
  compile target release 17. Docker Maven with Eclipse Temurin 17 passed.

## Risks And Follow-Ups

- This slice maps and seeds legacy table shape. It does not implement full
  runtime save/load behavior for arbitrary legacy view definitions.
