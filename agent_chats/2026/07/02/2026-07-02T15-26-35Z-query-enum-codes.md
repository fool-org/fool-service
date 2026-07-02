# Legacy Query Enum Codes

## Prompt

- Continue the Docker/Vue FoolFrame migration with timely atomic commits.

## Scope

- Migrated `OrderType` to legacy codes: `ASC(0)`, `DESC(1)`, `NULL(2)`.
- Migrated `AddQueryTable` to legacy codes:
  `Success(0)`, `NoRelation(1)`, `Exists(2)`.
- Migrated `JoinQueryType` to legacy codes:
  `Parent(0)`, `Items(1)`, `All(2)`.

## Changed Files

- `fool-query/src/main/java/org/fool/framework/query/OrderType.java`
- `fool-query/src/main/java/org/fool/framework/query/AddQueryTable.java`
- `fool-query/src/main/java/org/fool/framework/query/JoinQueryType.java`
- `fool-query/src/test/java/org/fool/framework/query/QueryEnumMigrationTest.java`
- `docs/migration/foolframe-parity.md`

## Validation

- Red check:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-query -am -Dtest=org.fool.framework.query.QueryEnumMigrationTest test`
  failed because `code()` did not exist on the query enums.
- Green check:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-query -am -Dtest=org.fool.framework.query.QueryEnumMigrationTest test`
- Module check:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-query -am test`
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

- This slice preserves public enum-code parity only. Saved-query persistence
  and query-to-view integration remain in the existing migration backlog.
