# Event Auto-SYSID Object ID Resolution

## Prompt

- Continue the goal to bring the Docker stack up, migrate FoolFrame parity, keep the frontend on Vue, and retry with full workspace permissions.

## Scope

- Compared the legacy `SCPB09-SOWAY.EVENT` object query path with `SCPB05-Soway.Model.SqlServer`.
- Legacy `dbContext.GetBySqlCommand` uses `SqlHelper.GetKeyCol(model)` and requires that key column in the query result.
- Legacy `GetKeyCol` returns `SYSID` when `model.AutoSysId == true` and `model.IdProperty == null`.
- Migrated that branch into `JdbcEventModelTableResolver` so event matched-object IDs use `SYSID` for auto-sys-id models without an explicit id property.

## Changed Files

- `fool-event/src/main/java/org/fool/framework/event/JdbcEventModelTableResolver.java`
- `fool-event/src/test/java/org/fool/framework/event/EventMigrationTest.java`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/06/24/2026-06-24T07-14-54Z-event-auto-sysid-object-id.md`

## Validation

- Red test first:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -q -pl fool-event -am -Dtest=EventMigrationTest#jdbcEventModelTableResolverUsesLegacySysIdForAutoSysIdModels -DfailIfNoTests=false test`
  - Failed with `expected:<[SYS]ID> but was:<[]ID>`.
- Focused green:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -q -pl fool-event -am -Dtest=EventMigrationTest#jdbcEventModelTableResolverUsesLegacySysIdForAutoSysIdModels -DfailIfNoTests=false test`
- Event regression:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -q -pl fool-event -am -Dtest=EventMigrationTest -DfailIfNoTests=false test`
- Harness and whitespace:
  - `python scripts/check_repo_harness.py`
  - `git diff --check`
- Docker rebuild and runtime smoke:
  - `docker compose up -d --build`
  - `docker compose ps`
  - `curl -fsS --retry 10 --retry-delay 3 --retry-connrefused http://localhost:8080/test`
  - `curl -fsS --retry 10 --retry-delay 3 --retry-connrefused http://localhost:8081/`
  - `curl -fsS --retry 10 --retry-delay 3 --retry-connrefused -H 'Content-Type: application/json' -d '{"viewName":"OrderList"}' http://localhost:8080/api/v1/view/get-view`
  - `curl -fsS --retry 10 --retry-delay 3 --retry-connrefused -H 'Content-Type: application/json' -d '{"viewName":"OrderList","pageInfo":{"pageSize":10,"pageIndex":1},"filter":null}' http://localhost:8080/api/v1/data/query-list`
  - `curl -fsS --retry 10 --retry-delay 3 --retry-connrefused -H 'Content-Type: application/json' -d '{"viewName":"OrderList","pageInfo":{"pageSize":10,"pageIndex":1},"filter":{"orderId":{"values":["1001","1002"]}}}' http://localhost:8080/api/v1/data/query-list`

## Skipped Checks

- Full backend `mvn test` was not rerun because this slice is isolated to `fool-event` metadata resolution and the Docker image build already ran with the repository's current `-DskipTests` image-build path.

## Risks And Follow-Ups

- Remaining event work still includes deeper dynamic object-query behavior beyond null-model handling, table/id-column resolution, row-value capture, and legacy filter SQL construction.
