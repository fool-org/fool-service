# Business Object Keyword Filter Parity

## Prompt

- Continue the active FoolFrame migration goal.
- Current slice: migrate legacy `ListViewQueryContext` keyword filtering for
  read-only BusinessObject list columns that display the target model
  `ShowProperty`.

## Scope

- `DataQueryService` now includes read-only BusinessObject list items in the
  keyword filter.
- Non-multi-map BusinessObject properties with a distinct target
  `showProperty` filter against the joined target alias and show column.
- BusinessObject properties whose show property is the target ID keep filtering
  against the base foreign-key column.
- Multi-map BusinessObject properties filter against the DBMap column for the
  target show property when present.
- Updated `docs/migration/foolframe-parity.md`.

## Changed Files

- `fool-view/src/main/java/org/fool/framework/view/service/DataQueryService.java`
- `fool-view/src/test/java/org/fool/framework/view/service/DataQueryServiceOrderingTest.java`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/02/2026-07-02T10-10-20Z-business-object-keyword-filter.md`

## Validation

- Red:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false -Dtest=DataQueryServiceOrderingTest test`
  failed with the expected missing BusinessObject keyword SQL:
  expected `` `customer`.`customer_name` LIKE ? `` but got only `1=1`.
- Green focused:
  the same command passed with `BUILD SUCCESS`;
  `DataQueryServiceOrderingTest` ran 4 tests with 0 failures and 0 errors.
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

- Ordering by joined BusinessObject show columns is still a separate parity
  gap.
