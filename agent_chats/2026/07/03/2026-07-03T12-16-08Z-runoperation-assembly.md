# runoperation Assembly Command Migration

## Prompt

- Continue the Docker/Vue FoolFrame migration and keep commits atomic.

## Scope

- Matched the executable part of FoolFrame `BaseOperationType.Assebmly` for
  Java classpath handlers.
- `CommandsType.SetConStrValue` now collects constructor arguments in command
  index order.
- `CommandsType.SetParamValue` now collects method arguments in command index
  order.
- `runoperation` now instantiates `SW_MODEL_OPERATION_INVOKECLASS` and invokes
  `SW_MODEL_OPERATION_INVOKEMETHOD` with the current `IDynamicData` plus the
  collected method arguments.
- Kept `Create`/`Update`/`Delete`, `SetValue`, `Filter`,
  direct/collection property-model method, and list-method behavior unchanged.

## Changed Files

- `fool-view/src/main/java/org/fool/framework/view/service/DataQueryService.java`
- `fool-view/src/test/java/org/fool/framework/view/service/DataQueryServiceRunOperationTest.java`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/03/2026-07-03T12-16-08Z-runoperation-assembly.md`

## Validation

- Red check:
  `docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false -Dtest=DataQueryServiceRunOperationTest test`
  - Failed as expected: `runLegacyAssemblyOperationInvokesJavaClassWithCollectedParams`
    returned unsuccessful because `ASSEBMLY` was still unsupported.
- Green focused check:
  `docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false -Dtest=DataQueryServiceRunOperationTest test`
  - Tests run: 17, failures: 0, errors: 0, skipped: 0.
- Green module check:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false test`
  - Tests run: 99, failures: 0, errors: 0, skipped: 0.

## Runtime Evidence

- Docker rebuild:
  `COMPOSE_PROGRESS=plain docker compose up -d --build backend`
  - Maven package succeeded across all 15 modules.
  - Backend image rebuilt and `fool-service-backend-1` restarted.
- Runtime smoke:
  `curl -fsS http://localhost:8080/test` and
  `curl -fsS http://localhost:8081/test`
  - Both returned seeded order rows, including IDs `1`, `2`, and `3` in the
    response prefix.

## Skipped Checks

- Frontend unit/build checks were not rerun because this slice does not change
  Vue source.
- No Docker seed route currently invokes a Java classpath assembly handler; the
  executable behavior is covered by the focused unit test.

## Remaining Risk

- `SW_MODEL_OPERATION_INVOKEDLL` is not loaded; the migrated path supports
  classes already on the JVM classpath.
- WCF/JSON operation types, external-model operation execution, operation
  triggers, and routed-connection transaction behavior remain future work.
