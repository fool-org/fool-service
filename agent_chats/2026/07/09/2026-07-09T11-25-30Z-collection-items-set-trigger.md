# Collection ItemsSet Trigger

## Prompt

- Continue the Docker/FoolFrame/Vue migration, keeping the implementation
  small and shared.

## Scope

- Added legacy `PropertyTriggerType.ITEMS_SET` execution for existing owned
  child rows in `ModelDataService.writeOwnedCollection`.
- Extended the existing collection trigger test so add, set, and delete trigger
  paths are covered together.
- Updated migration/task state.

## Changed Files

- `fool-model/src/main/java/org/fool/framework/model/service/ModelDataService.java`
- `fool-model/src/test/java/org/fool/framework/model/service/ModelDataServiceTest.java`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/09/2026-07-09T11-25-30Z-collection-items-set-trigger.md`

## Validation

- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-model -am -Dtest=ModelDataServiceTest#saveDataExecutesLegacyCollectionItemTriggers test` - passed.
- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-model -am -Dtest=ModelDataServiceTest test` - passed, 34 tests.
- `python scripts/check_repo_harness.py` - passed.
- `python scripts/runtime_doctor.py` - passed.
- `git diff --check` - passed.

## Skipped Checks

- Host Maven was not used because the local JDK still fails Java 17 compilation
  with `invalid target release: 17`.

## Risks

- Low. The change is one shared trigger call on the existing child-update
  branch; add/delete behavior remains on the same paths and is covered by the
  expanded test.
