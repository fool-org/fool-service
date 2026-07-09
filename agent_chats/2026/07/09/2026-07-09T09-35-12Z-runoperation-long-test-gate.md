# 2026-07-09 Runoperation Long Test Gate

## Prompt

- Continue the Docker/FoolFrame/Vue migration goal.
- Keep changes small, reuse existing migration behavior, and commit atomically.

## Scope

- Reproduced the broader Maven failure in
  `DataQueryServiceRunOperationTest.runLegacyUpdateOperationConvertsStaticSetValueScalarTypes`.
- Confirmed the target property is `PropertyType.Long`, while the shared
  `OperationCommandValueResolver` and model resolver tests intentionally map
  legacy Long static SetValue expressions to Java `Long`.
- Updated the stale view-layer test expectation from `Integer<123>` to
  `Long<123>` without changing production conversion logic.

## Changed Files

- `fool-view/src/test/java/org/fool/framework/view/service/DataQueryServiceRunOperationTest.java`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/09/2026-07-09T09-35-12Z-runoperation-long-test-gate.md`

## Red Test

- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -Dtest=DataQueryServiceRunOperationTest#runLegacyUpdateOperationConvertsStaticSetValueScalarTypes test`
  failed first with `expected: java.lang.Integer<123> but was: java.lang.Long<123>`.

## Validation

- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -Dtest=DataQueryServiceRunOperationTest#runLegacyUpdateOperationConvertsStaticSetValueScalarTypes test` passed.
- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-model -am -Dtest=OperationCommandValueResolverTest test` passed.
- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-app-manage -am -DfailIfNoTests=false test` passed.

## Risks

- This is a test expectation correction only. It does not change runtime
  command conversion behavior.
