# Legacy Load And Save Type Codes

## Prompt

- Continue the Docker/Vue FoolFrame migration and make timely atomic commits.

## Scope

- Added `LoadType` with legacy null/partial/complete/no-object codes.
- Added `SaveType` with legacy unknown/exists/unexists codes.

## Changed Files

- `fool-model/src/main/java/org/fool/framework/model/model/LoadType.java`
- `fool-model/src/main/java/org/fool/framework/model/model/SaveType.java`
- `fool-model/src/test/java/org/fool/framework/model/model/LoadSaveTypeMigrationTest.java`
- `docs/migration/foolframe-parity.md`

## Validation

- Red check:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-model -am -Dtest=org.fool.framework.model.model.LoadSaveTypeMigrationTest test`
  failed because `LoadType` and `SaveType` did not exist.
- Green check:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-model -am -Dtest=org.fool.framework.model.model.LoadSaveTypeMigrationTest test`
- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-model -am test`
- `python scripts/check_repo_harness.py`
- `git diff --check`
- `curl -sS -m 5 http://localhost:8080/test`
- `curl -sS -m 5 -H 'Content-Type: application/json' -d '{"viewName":"OrderList"}' http://localhost:8080/api/v1/view/get-view`
- `curl -sS -m 5 -H 'Content-Type: application/json' -d '{"viewName":"OrderList","pageInfo":{"pageSize":10,"pageIndex":1},"filter":null}' http://localhost:8080/api/v1/data/query-list`
- `curl -sS -m 5 http://localhost:8081/ | head -5`
- `docker compose ps`

## Risks And Follow-Ups

- This slice preserves enum value parity only. Runtime load/save state handling
  remains outside this slice.
