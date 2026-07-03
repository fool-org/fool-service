# Collection Item Triggers

## Prompt

Continue the Docker/FoolFrame/Vue migration while keeping the implementation
small and reusable.

## Scope

- Compare FoolFrame collection add/delete trigger behavior against the dynamic
  Java save path.
- Execute migrated `SET_VALUE` command side effects for collection
  `ItemsAdd` / `ItemsDelete` property triggers.

## Changes

- `ModelDataService` now runs `ItemsAdd` property triggers before inserting a
  new owned child row or relation-table row.
- `ModelDataService` now runs `ItemsDelete` property triggers before deleting
  an owned child row or relation-table row.
- Added a focused `ModelDataServiceTest` case proving a One2Many add mutates
  the inserted row and delete mutates the removed item before deletion.
- Updated `tasks.md` and the migration parity ledger.

## Validation

- Passed: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-model -am -DfailIfNoTests=false -Dtest=ModelDataServiceTest#saveDataExecutesLegacyCollectionItemTriggers test`.
- Passed: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-model -am -DfailIfNoTests=false -Dtest=ModelDataServiceTest test`.
- Passed: `python3 scripts/check_repo_harness.py`.
- Passed: `git diff --check`.

## Runtime Evidence

- Not rerun yet for this backend slice. The focused Maven test covers the new
  dynamic collection trigger behavior.

## Risks

- This only covers the migrated `SET_VALUE` command path. Other property
  trigger command types remain in migration backlog.

## Follow-ups

- Continue with the View-render-first/data-query-second frontend cleanup.
