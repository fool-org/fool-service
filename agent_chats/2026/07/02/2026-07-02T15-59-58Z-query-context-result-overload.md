# QueryContext Legacy Result Overload

## Prompt

- Continue the Docker/Vue FoolFrame migration with timely atomic commits.

## Scope

- Added the legacy `QueryContext.GetResult(connectionString, pageSize)`
  compatibility surface.
- Reused the existing `JdbcQueryExecutor` paging path and kept the stored
  context connection string stable across per-call connection-string overloads.
- Updated migration parity notes without marking the remaining query migration
  complete.

## Changed Files

- `fool-query/src/main/java/org/fool/framework/query/QueryContext.java`
- `fool-query/src/test/java/org/fool/framework/query/QueryContextTest.java`
- `docs/migration/foolframe-parity.md`

## Validation

- Red check:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-query -am -Dtest=QueryContextTest test`
  failed because `getResult(String, int)` did not exist.
- Green check:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-query -am -Dtest=QueryContextTest test`
  passed: 10 tests, 0 failures, 0 errors.
- Module check:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-query -am test`
  passed: `fool-query` 61 tests, 0 failures, 0 errors. Dependency
  modules also passed; `fool-dao` still logs its existing tested
  `DaoServiceTest` null-object stack trace while Surefire passes.
- Harness check:
  `python scripts/check_repo_harness.py`
  passed.
- Whitespace check:
  `git diff --check`
  passed.
- Runtime state:
  `docker compose ps`
  showed backend/frontend up and MySQL healthy.
- Runtime smoke:
  `curl -sS -m 5 http://localhost:8080/test`
  `curl -sS -m 5 -H 'Content-Type: application/json' -d '{"viewName":"OrderList"}' http://localhost:8080/api/v1/view/get-view`
  `curl -sS -m 5 -H 'Content-Type: application/json' -d '{"viewName":"OrderList","pageInfo":{"pageSize":10,"pageIndex":1},"filter":null}' http://localhost:8080/api/v1/data/query-list`
  `curl -sS -m 5 http://localhost:8081/`
  passed.

## Risks And Follow-Ups

- This preserves the legacy overload surface only. Runtime connection selection
  still belongs to the configured `JdbcQueryExecutor`/`JdbcTemplate` boundary.
