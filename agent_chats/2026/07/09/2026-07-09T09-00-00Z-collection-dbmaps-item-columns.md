# Collection DBMaps Item Columns

## Prompt

- Continue the FoolFrame migration with Docker/Vue as the target, maximize
  reuse, avoid concrete DTO shortcuts, and keep commits atomic.

## Scope

- Fix the shared collection item SQL generator so child rows with legacy
  DBMaps/multi-column BusinessObject snapshot properties select the mapped
  columns.
- Reuse the existing `Mapper` DBMaps alias contract instead of adding a second
  mapping path.

## Changed Files

- `fool-model/src/main/java/org/fool/framework/model/sqlscript/SqlGenerator.java`
- `fool-model/src/test/java/org/fool/framework/model/sqlscript/SqlGeneratorTest.java`
- `docs/migration/foolframe-parity.md`
- `tasks.md`

## Validation

- Red:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-model -am -Dtest=SqlGeneratorTest#generateItemsSelectsLegacyMultiDbMapColumnsForCollectionRows test`
  failed because `generateItems` omitted the child row DBMaps columns.
- Green:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-model -am -Dtest=SqlGeneratorTest#generateItemsSelectsLegacyMultiDbMapColumnsForCollectionRows test`
  passed.
- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-model -am -DfailIfNoTests=false test`
  passed: 80 tests, 0 failures, 0 errors.
- `python scripts/check_repo_harness.py` passed.
- `git diff --check` passed.
- `docker compose ps` showed backend, frontend, MySQL, and Redis running.

## Risks And Follow-Ups

- This covers item-query column selection only. Deeper DBMaps runtime/query
  behavior remains tracked in the migration document.
