# Trigger Method Commands

## Prompt

Continue the FoolFrame migration while maximizing reuse and avoiding larger
frontend/backend rewrites.

## Scope

- Added shared trigger-command support in `ModelDataService` for
  `CommandsType.ExuteProprtyModelMethod` and `CommandsType.ExuteListMethod`.
- Reused `IDynamicData.invoke` for direct dynamic property values and
  collection item values.
- Reused a no-arg method call for list objects, matching the migrated
  `runoperation` list-method behavior without introducing a new abstraction.
- Added focused coverage for model SAVE triggers invoking direct property
  methods, collection property-item methods, and list no-arg methods.

## Changed Files

- `fool-model/src/main/java/org/fool/framework/model/service/ModelDataService.java`
- `fool-model/src/test/java/org/fool/framework/model/service/ModelDataServiceTest.java`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/09/2026-07-09T07-52-42Z-trigger-method-commands.md`

## Validation

- Failing-before evidence:
  - `ModelDataServiceTest#saveDataExecutesLegacyTriggerPropertyAndListMethods`
    failed with `expected:<Close> but was:<null>`.
- Passing focused check:
  - `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-model -am -DfailIfNoTests=false -Dtest=ModelDataServiceTest#saveDataExecutesLegacyTriggerPropertyAndListMethods test`
  - Passed: BUILD SUCCESS, Tests run: 1, Failures: 0.
- Module check:
  - `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-model -am -DfailIfNoTests=false test`
  - Passed: BUILD SUCCESS, Tests run: 72, Failures: 0.
- `python scripts/check_repo_harness.py`
  - Passed: Repository harness validation passed.
- `git diff --check`
  - Passed.

## Risks

- This does not implement remaining trigger command types such as external
  model command recursion.
- This does not change routed-connection transaction behavior.
