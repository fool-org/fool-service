# Model Collection Relation Query

## Prompt

- Continue the active FoolFrame migration goal.
- Current slice: migrate collection item query SQL beyond simple foreign keys.

## Scope

- `SqlGenerator.generateItems` now uses legacy relation metadata for
  One2Many, Many2Many, and Recurve collection item queries.
- Collection item result rows expose a stable parent-id alias for
  `ModelDataService` bucketing.
- Updated `docs/migration/foolframe-parity.md`.

## Changed Files

- `fool-model/src/main/java/org/fool/framework/model/sqlscript/SqlGenerator.java`
- `fool-model/src/main/java/org/fool/framework/model/service/ModelDataService.java`
- `fool-model/src/test/java/org/fool/framework/model/sqlscript/SqlGeneratorTest.java`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/02/2026-07-02T10-59-55Z-model-collection-relation-query.md`

## Validation

- Focused model SQL:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-model -am -DfailIfNoTests=false -Dtest=SqlGeneratorTest test`
  passed with 9 tests, 0 failures, and 0 errors.
- Full backend:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false test`
  passed across all 15 Maven modules.
- Repo harness:
  `python scripts/check_repo_harness.py`
  passed.
- Whitespace:
  `git diff --check`
  passed.
- Docker runtime:
  `docker compose ps`
  showed backend/frontend up and MySQL/Redis healthy.

## Risks

- This only migrates relation-aware item query SQL and parent bucketing.
  Deeper DBMaps query/runtime behavior remains open.
