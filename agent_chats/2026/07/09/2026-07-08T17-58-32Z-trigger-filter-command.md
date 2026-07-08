# Trigger Filter Command

## Prompt

Continue the FoolFrame migration with Docker/Vue in place, keep commits
atomic, maximize reuse, and keep protocol/runtime behavior aligned with the
legacy source.

## Scope

- Compared FoolFrame `ModelMethodContext.ExcuteOperation` / `CheckCommand`
  with the migrated `ModelDataService` trigger execution path.
- Added `CommandsType.FILTER` execution to the shared trigger-command path for
  model, property, and collection item triggers.
- A failed trigger filter now checks the current persisted row by model ID
  column and raw command expression, throws the legacy command message, and
  stops later trigger commands/base operations.
- Updated parity docs and task state.

## Changed Files

- `fool-model/src/main/java/org/fool/framework/model/service/ModelDataService.java`
- `fool-model/src/test/java/org/fool/framework/model/service/ModelDataServiceTest.java`
- `docs/migration/foolframe-parity.md`
- `tasks.md`

## Validation

- Red: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-model -am -Dtest=ModelDataServiceTest#saveDataStopsLegacySaveTriggerWhenFilterCommandDoesNotMatch test` failed because no `IllegalStateException` was thrown.
- Green: same focused Maven command passed after implementing trigger `FILTER`.
- Module: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-model -am test` passed.
- View integration surface: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-view -am test` passed.
- Harness: `python scripts/check_repo_harness.py` passed.
- Whitespace: `git diff --check` passed.

## Runtime Evidence

`docker compose ps` showed MySQL, Redis, backend, and frontend up. No Docker
runtime doctor was used for this slice. The running backend container was not
rebuilt with this code, so a runtime doctor run would not prove the new trigger
behavior.

## Risks

- Raw trigger filter SQL remains trusted metadata, matching the existing
  migrated raw View/filter paths. It should only come from migrated legacy
  metadata.

## Follow-ups

- Continue remaining trigger/runtime command parity for non-`Filter` command
  types and operation-trigger side effects.
