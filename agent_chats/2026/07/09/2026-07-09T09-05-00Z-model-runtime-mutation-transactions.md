# Model Runtime Mutation Transactions

## Prompt

- Continue the FoolFrame migration with Docker/Vue as the target, maximize
  reuse, avoid concrete DTO shortcuts, and keep commits atomic.

## Scope

- Align the shared `ModelDataService` mutation path with FoolFrame
  `dbContext` same-connection transaction behavior.
- Preserve FoolFrame model-trigger ordering: `Create` / `Save` triggers after
  the write transaction and `Delete` triggers before the delete transaction.
- Keep the fix at the public model write entrypoints instead of adding
  per-collection guards.

## Changed Files

- `fool-model/src/main/java/org/fool/framework/model/service/ModelDataService.java`
- `fool-model/src/test/java/org/fool/framework/model/service/ModelDataServiceTest.java`
- `docs/migration/foolframe-parity.md`
- `tasks.md`

## Validation

- Red:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-model -am -Dtest=ModelDataServiceTest#saveDataRollsBackLegacyParentWhenOwnedChildWriteFails test`
  failed because the parent row stayed updated after a child insert failure.
- Green:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-model -am -Dtest=ModelDataServiceTest#saveDataRollsBackLegacyParentWhenOwnedChildWriteFails test`
  passed.
- Regression pair:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-model -am -Dtest=ModelDataServiceTest#saveDataRollsBackLegacyParentWhenOwnedChildWriteFails+saveDataStopsLegacySaveTriggerWhenFilterCommandDoesNotMatch test`
  passed.
- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-model -am -DfailIfNoTests=false test`
  passed: 81 tests, 0 failures, 0 errors.
- `python scripts/check_repo_harness.py` passed.
- `git diff --check` passed.
- `docker compose ps` showed backend, frontend, MySQL, and Redis running.

## Risks And Follow-Ups

- This covers same-datasource public model writes. Cross/routed-connection
  transaction behavior remains tracked in the migration document.
