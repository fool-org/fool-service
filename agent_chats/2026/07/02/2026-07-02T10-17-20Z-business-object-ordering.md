# Business Object Ordering Parity

## Prompt

- Continue the active FoolFrame migration goal.
- Current slice: migrate legacy list-query ordering for BusinessObject list
  columns whose target model display value is `ShowProperty`.

## Scope

- `DataQueryService` now passes the joined BusinessObject show-property column
  expression when the first `ShowIndex` item is a BusinessObject.
- `SqlGenerator` now accepts already-qualified order expressions without
  wrapping the whole expression in another pair of backticks.
- List count SQL can include the same BusinessObject joins used by list select
  SQL, so keyword filters over joined show-property columns work during paging
  count queries.
- Multi-map and ID-showing BusinessObject order paths continue to fall back to
  the local mapped column.
- Updated `docs/migration/foolframe-parity.md`.

## Changed Files

- `fool-view/src/main/java/org/fool/framework/view/service/DataQueryService.java`
- `fool-view/src/test/java/org/fool/framework/view/service/DataQueryServiceOrderingTest.java`
- `fool-model/src/main/java/org/fool/framework/model/sqlscript/SqlGenerator.java`
- `fool-model/src/main/java/org/fool/framework/model/service/ModelDataService.java`
- `fool-model/src/test/java/org/fool/framework/model/sqlscript/SqlGeneratorTest.java`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/02/2026-07-02T10-17-20Z-business-object-ordering.md`

## Validation

- Red:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false -Dtest=DataQueryServiceOrderingTest test`
  failed because ordering still passed `customer_id` instead of
  `` `customer`.`customer_name` ``.
- Red:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-model -am -DfailIfNoTests=false -Dtest=SqlGeneratorTest test`
  failed because the order expression became
  `` ``customer`.`customer_name`` ``.
- Green focused:
  both focused commands passed with `BUILD SUCCESS`;
  `DataQueryServiceOrderingTest` ran 5 tests and `SqlGeneratorTest` ran 5 tests
  with 0 failures and 0 errors.
- Full backend:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false test`
  passed with `BUILD SUCCESS` across the 15-module reactor.
- Repository harness:
  `python scripts/check_repo_harness.py` passed.
- Whitespace:
  `git diff --check` passed with no output.
- Runtime stack:
  `docker compose ps` showed backend and frontend up, with MySQL and Redis
  healthy.

## Risks

- Richer explicit user-selected ordering is not exposed yet; this slice covers
  the legacy default list ordering path.
