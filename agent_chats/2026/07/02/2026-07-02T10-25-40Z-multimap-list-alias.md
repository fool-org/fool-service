# Multi-Map List Alias Parity

## Prompt

- Continue the active FoolFrame migration goal.
- Current slice: migrate legacy list-query aliasing for multi-map
  BusinessObject DBMaps.

## Scope

- `SqlGenerator` now emits legacy-style list-query aliases for multi-map
  BusinessObject DBMaps as `propertyName_targetPropertyName`.
- `Mapper` now reads those aliases and keeps the previous raw DB column fallback
  for older non-aliased selects.
- Updated `docs/migration/foolframe-parity.md`.

## Changed Files

- `fool-model/src/main/java/org/fool/framework/model/sqlscript/SqlGenerator.java`
- `fool-model/src/main/java/org/fool/framework/model/service/Mapper.java`
- `fool-model/src/test/java/org/fool/framework/model/sqlscript/SqlGeneratorTest.java`
- `fool-model/src/test/java/org/fool/framework/model/service/MapperDbMapsTest.java`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/02/2026-07-02T10-25-40Z-multimap-list-alias.md`

## Validation

- Red:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-model -am -DfailIfNoTests=false -Dtest=SqlGeneratorTest,MapperDbMapsTest test`
  failed because multi-map list SQL selected raw DB columns and `Mapper` did
  not read `customer_customerId` / `customer_displayName` aliases.
- Green focused:
  the same command passed with `BUILD SUCCESS`; `SqlGeneratorTest` ran 6 tests
  and `MapperDbMapsTest` ran 5 tests with 0 failures and 0 errors.
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

- This slice covers list-query DBMaps projection/mapping. Deeper DBMaps query
  behavior outside list-query alias mapping remains open.
