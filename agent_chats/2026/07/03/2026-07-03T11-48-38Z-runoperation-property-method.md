# runoperation Property Method Command Migration

## Prompt

- Continue the Docker/Vue FoolFrame migration and keep commits atomic.

## Scope

- Matched the direct-property branch of FoolFrame
  `CommandsType.ExuteProprtyModelMethod`.
- When a `runoperation` command targets a property whose current value is an
  `IDynamicData`, the migrated service now invokes the command expression as a
  method name before the base operation save/delete/create step.
- Kept existing `SetValue`, `Filter`, and base operation behavior unchanged.

## Changed Files

- `fool-view/src/main/java/org/fool/framework/view/service/DataQueryService.java`
- `fool-view/src/test/java/org/fool/framework/view/service/DataQueryServiceRunOperationTest.java`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/03/2026-07-03T11-48-38Z-runoperation-property-method.md`

## Validation

- Red check:
  `docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false -Dtest=DataQueryServiceRunOperationTest test`
  - Failed as expected: `iDynamicData.invoke("Deactivate")` was not invoked.
- Green focused check:
  `docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false -Dtest=DataQueryServiceRunOperationTest test`
  - Tests run: 14, failures: 0, errors: 0.
- Green module check:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false test`
  - Tests run: 96, failures: 0, errors: 0.

## Runtime Evidence

- Docker rebuild:
  `COMPOSE_PROGRESS=plain docker compose up -d --build backend`
  - Backend image rebuilt successfully and `fool-service-backend-1`
    restarted.
- Runtime smoke:
  `curl -fsS http://localhost:8080/test` and
  `curl -fsS http://localhost:8081/test`
  - Both returned seeded order rows, including IDs `1`, `2`, and `3` in the
    response prefix.

## Skipped Checks

- Frontend unit/build checks were not rerun because this slice does not change
  Vue source.
- No Docker seed route currently exercises an `IDynamicData.invoke` side effect
  for `ExuteProprtyModelMethod`; runtime smoke is limited to backend readiness.

## Remaining Risk

- Collection/list property method execution is not implemented.
- External-model operation execution, constructor/parameter command collection,
  assembly/WCF/JSON paths, and operation triggers remain future work.
