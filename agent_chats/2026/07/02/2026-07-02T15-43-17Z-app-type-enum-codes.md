# Legacy AppType Enum Codes

## Prompt

- Continue the Docker/Vue FoolFrame migration with timely atomic commits.

## Scope

- Migrated app-management `AppType` to explicit legacy codes:
  `Web(0)`, `WinForm(1)`, `Android(2)`, `iOS(3)`, `Service(4)`,
  and `Sensor(5)`.
- Kept application table mapping and DAO enum-code behavior unchanged.

## Changed Files

- `fool-app-manage/src/main/java/org/fool/framework/app/AppType.java`
- `fool-app-manage/src/test/java/org/fool/framework/app/AppManageMigrationTest.java`
- `docs/migration/foolframe-parity.md`

## Validation

- Red check:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-app-manage -am -Dtest=org.fool.framework.app.AppManageMigrationTest#appModelsKeepLegacyTableMappings test`
  failed because `AppType.code()` did not exist.
- Green compile check:
  same focused command passed compilation, but matched `Tests run: 0`, so it
  was not accepted as sufficient validation.
- Green test:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-app-manage -am -Dtest=AppManageMigrationTest test`
  ran `AppManageMigrationTest`: 38 tests, 0 failures, 0 errors.
- Module check:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-app-manage -am test`
  passed: 40 app-manage tests plus dependency module tests.
- Harness check:
  `python scripts/check_repo_harness.py`
- Whitespace check:
  `git diff --check`
- Runtime smoke:
  `curl -sS -m 5 http://localhost:8080/test`
  `curl -sS -m 5 -H 'Content-Type: application/json' -d '{"viewName":"OrderList"}' http://localhost:8080/api/v1/view/get-view`
  `curl -sS -m 5 -H 'Content-Type: application/json' -d '{"viewName":"OrderList","pageInfo":{"pageSize":10,"pageIndex":1},"filter":null}' http://localhost:8080/api/v1/data/query-list`
  `curl -sS -m 5 http://localhost:8081/`
  `docker compose ps`

## Risks And Follow-Ups

- This slice preserves persisted app-type enum parity only. Broader
  AppInstall transaction and DBMaps runtime parity remain in the migration
  backlog.
