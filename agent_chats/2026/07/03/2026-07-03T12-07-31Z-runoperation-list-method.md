# runoperation List Method Command Migration

## Prompt

- Continue the Docker/Vue FoolFrame migration and keep commits atomic.
- Report the overall migration completion percentage.

## Scope

- Matched FoolFrame `CommandsType.ExuteListMethod` for list proxy objects.
- When a `runoperation` command targets a property whose current value exposes
  the named no-arg method, the migrated service invokes that method before the
  base operation save/delete/create step.
- `IDynamicData` list proxies are also supported through their existing
  `invoke` surface.
- Kept existing `SetValue`, `Filter`, direct/collection property-model method,
  and base operation behavior unchanged.

## Changed Files

- `fool-view/src/main/java/org/fool/framework/view/service/DataQueryService.java`
- `fool-view/src/test/java/org/fool/framework/view/service/DataQueryServiceRunOperationTest.java`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/03/2026-07-03T12-07-31Z-runoperation-list-method.md`

## Validation

- Red check:
  `docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false -Dtest=DataQueryServiceRunOperationTest test`
  - Failed as expected: `runLegacyUpdateOperationInvokesListMethodCommandOnCollectionObject`
    did not close the target list object.
- Green focused check:
  `docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false -Dtest=DataQueryServiceRunOperationTest test`
  - Tests run: 16, failures: 0, errors: 0, skipped: 0.
- Green module check:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false test`
  - Tests run: 98, failures: 0, errors: 0, skipped: 0.

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
- No Docker seed route currently exercises a list-method side effect; runtime
  smoke is limited to backend/Vue readiness.

## Remaining Risk

- External-model operation execution, constructor/parameter command collection,
  assembly/WCF/JSON paths, and operation triggers remain future work.
- Richer Java list-proxy parity remains limited until the migrated runtime has
  a first-class proxy equivalent to FoolFrame dynamic list objects.
