# Legacy Operation Base Type Codes

## Prompt

- Continue the Docker/Vue FoolFrame migration with timely atomic commits.

## Scope

- Migrated `OperationBaseType` to legacy `BaseOperationType` codes.
- Preserved the legacy `Assebmly` spelling as `ASSEBMLY`.
- Changed new `Operation` default base type to legacy `NULL`.

## Changed Files

- `fool-model/src/main/java/org/fool/framework/model/model/OperationBaseType.java`
- `fool-model/src/main/java/org/fool/framework/model/model/Operation.java`
- `fool-model/src/test/java/org/fool/framework/model/model/OperationEnumMigrationTest.java`
- `docs/migration/foolframe-parity.md`

## Validation

- Red check:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-model -am -Dtest=org.fool.framework.model.model.OperationEnumMigrationTest test`
  failed because `OperationBaseType.code()`, `NULL`, `ASSEBMLY`, `WCF`,
  `JSONPOST`, and `JSONGET` did not exist.
- Green check:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-model -am -Dtest=org.fool.framework.model.model.OperationEnumMigrationTest test`
- Module check:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-view -am test`
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

- This slice preserves persisted enum value parity only. Operation invocation
  behavior beyond existing create/update/delete/default-view handling remains
  outside this slice.
