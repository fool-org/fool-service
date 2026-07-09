# Model Trigger Assembly

## Prompt

Continue the Docker/Vue/FoolFrame migration with maximum reuse and atomic
commits.

## Scope

- Added model-trigger `BaseOperationType.Assebmly` execution in
  `ModelDataService`.
- Collected trigger `SetParamValue` and `SetConStrValue` command values before
  invoking the assembly handler.
- Moved the Java classpath assembly invocation helper into `fool-model` and
  reused it from `fool-view` `runoperation`, avoiding two reflection paths.
- Left `invokeDll` plugin loading deferred, matching the existing migrated
  `runoperation` boundary.

## Changed Files

- `fool-model/src/main/java/org/fool/framework/model/service/LegacyAssemblyInvoker.java`
- `fool-model/src/main/java/org/fool/framework/model/service/ModelDataService.java`
- `fool-model/src/test/java/org/fool/framework/model/service/ModelDataServiceTest.java`
- `fool-view/src/main/java/org/fool/framework/view/service/DataQueryService.java`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/09/2026-07-09T07-59-09Z-model-trigger-assembly.md`

## Validation

- Failing-before evidence:
  - `ModelDataServiceTest#saveDataExecutesLegacyModelTriggerAssembly` failed
    with `expected:<ctor> but was:<null>`.
- Passing focused checks:
  - `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-model -am -DfailIfNoTests=false -Dtest=ModelDataServiceTest#saveDataExecutesLegacyModelTriggerAssembly test`
  - Passed: BUILD SUCCESS, Tests run: 1, Failures: 0.
  - `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false -Dtest=DataQueryServiceRunOperationTest#runLegacyAssemblyOperationInvokesJavaClassWithCollectedParams test`
  - Passed: BUILD SUCCESS, Tests run: 1, Failures: 0.
- Broader validation:
  - `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false test`
  - Passed: BUILD SUCCESS, Tests run: 156, Failures: 0.
  - `python scripts/check_repo_harness.py`
  - Passed: Repository harness validation passed.
  - `git diff --check`
  - Passed.

## Risks

- Remaining trigger command types beyond the shared command slices remain open.
- Routed-connection transaction behavior remains open.
