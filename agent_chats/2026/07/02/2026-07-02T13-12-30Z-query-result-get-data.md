# Query Result Get Data

## Prompt

Continue the Docker/FoolFrame/Vue migration goal and report the current
migration percentage.

## Scope

- Added the legacy `QueryResult.GetData` current-page data surface as
  `QueryResult#getData()`.
- Bound the alias to the existing Java paged-result row payload.
- Updated the migration parity document for the covered SWDQ01 query surface.

## Changed Files

- `fool-query/src/main/java/org/fool/framework/query/QueryResult.java`
- `fool-query/src/test/java/org/fool/framework/query/QueryInstanceMigrationTest.java`
- `docs/migration/foolframe-parity.md`

## Validation

- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-query -am -Dtest=QueryInstanceMigrationTest test`
- `python scripts/check_repo_harness.py`
- `curl -sS -m 5 http://localhost:8080/test`
- `curl -sS -m 5 -H 'Content-Type: application/json' -d '{"viewName":"OrderList"}' http://localhost:8080/api/v1/view/get-view`
- `curl -sS -m 5 http://localhost:8081/ | head -5`
- `docker compose ps`
- `git diff --check`

## Skipped Checks

- Full root `mvn test` was not run; this slice only touches `fool-query`
  `QueryResult` API surface and its focused migration test.

## Risks And Follow-Ups

- Saved-query/report execution and richer query-to-view integration remain
  open SWDQ01 migration work.
