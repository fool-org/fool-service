# Legacy Operation Command And Order Type Codes

## Prompt

- Continue the Docker/Vue FoolFrame migration and make timely atomic commits.

## Scope

- Added `OperationType` with legacy `Danymic`/`Reflection` codes.
- Added `CommandsType` with legacy command codes and original misspellings.
- Added `OrderByType` with legacy ascending/descending codes.

## Changed Files

- `fool-model/src/main/java/org/fool/framework/model/model/OperationType.java`
- `fool-model/src/main/java/org/fool/framework/model/model/CommandsType.java`
- `fool-model/src/main/java/org/fool/framework/model/model/OrderByType.java`
- `fool-model/src/test/java/org/fool/framework/model/model/OperationEnumMigrationTest.java`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/02/2026-07-02T14-55-26Z-operation-command-order-type-codes.md`

## Validation

- Red check:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-model -am -Dtest=org.fool.framework.model.model.OperationEnumMigrationTest test`
  failed because `OperationType`, `CommandsType`, and `OrderByType` did not
  exist.
- Green check:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-model -am -Dtest=org.fool.framework.model.model.OperationEnumMigrationTest test`
- Module check:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-model -am test`
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

- This slice preserves enum value parity only. Command execution and operation
  invocation remain outside this slice.
