# Legacy Connection Type Codes

## Prompt

- Continue the Docker/Vue FoolFrame migration and make timely atomic commits.

## Scope

- Added `ConnectionType` with legacy `Default`/`System`/`AppSys`/`Current`/`ModelSys`
  codes.
- Aligned app-install connection type constants with the migrated enum codes.

## Changed Files

- `fool-model/src/main/java/org/fool/framework/model/model/ConnectionType.java`
- `fool-app-manage/src/main/java/org/fool/framework/app/AppInstalledModel.java`
- `fool-app-manage/src/main/java/org/fool/framework/app/AppSystemView.java`
- `fool-app-manage/src/test/java/org/fool/framework/app/ConnectionTypeMigrationTest.java`
- `docs/migration/foolframe-parity.md`

## Validation

- Red check:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-app-manage -am -Dtest=org.fool.framework.app.ConnectionTypeMigrationTest test`
  failed because `ConnectionType` did not exist.
- Green check:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-app-manage -am -Dtest=org.fool.framework.app.ConnectionTypeMigrationTest test`
- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-app-manage -am test`
- `python scripts/check_repo_harness.py`
- `git diff --check`
- `curl -sS -m 5 http://localhost:8080/test`
- `curl -sS -m 5 -H 'Content-Type: application/json' -d '{"viewName":"OrderList"}' http://localhost:8080/api/v1/view/get-view`
- `curl -sS -m 5 -H 'Content-Type: application/json' -d '{"viewName":"OrderList","pageInfo":{"pageSize":10,"pageIndex":1},"filter":null}' http://localhost:8080/api/v1/data/query-list`
- `curl -sS -m 5 http://localhost:8081/ | head -5`
- `docker compose ps`

## Risks And Follow-Ups

- This slice preserves the legacy connection type value surface only. Broader
  routed transaction behavior remains outside this slice.
