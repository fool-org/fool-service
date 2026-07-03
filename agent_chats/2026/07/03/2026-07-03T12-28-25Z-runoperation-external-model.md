# runoperation External Model Command Migration

## Prompt

- Continue the Docker/Vue FoolFrame migration and keep commits atomic.
- Report the overall migration completion percentage.

## Scope

- Matched the executable part of FoolFrame
  `CommandsType.ExuteOutModelMethod` for external model operations.
- `runoperation` now loads the target model from
  `SW_SYS_COMMAND_ARGMODEL`, resolves the target operation name from
  `SW_SYS_COMMAND_EXP` case-insensitively, and resolves the target object ID
  from `SW_SYS_COMMAND_ARGID`.
- Target create/update/delete operations execute their command list using the
  source object as the value source, matching FoolFrame's external operation
  helper behavior for `SetValue`.
- The returned target data can be mapped back to the source object through
  `SW_SYS_COMMAND_ARGEXP` before the source base operation persists.
- Kept existing create/update/delete, `SetValue`, `Filter`,
  direct/collection property-model method, list-method, and Java classpath
  assembly behavior unchanged.

## Changed Files

- `fool-view/src/main/java/org/fool/framework/view/service/DataQueryService.java`
- `fool-view/src/test/java/org/fool/framework/view/service/DataQueryServiceRunOperationTest.java`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/03/2026-07-03T12-28-25Z-runoperation-external-model.md`

## Validation

- Red check:
  `docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false -Dtest=DataQueryServiceRunOperationTest test`
  - Failed as expected:
    `runLegacyUpdateOperationExecutesOutModelCommandAndMapsResult` expected
    the shipment status to be mapped from the source object, but it remained
    `pending`.
- Green focused check:
  `docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -v /tmp:/host-tmp -w /workspace maven:3.9-eclipse-temurin-17 sh -lc 'mvn -pl fool-view -am -DfailIfNoTests=false -Dtest=DataQueryServiceRunOperationTest test > /host-tmp/fool-external-focused-final.log 2>&1; status=$?; tail -n 50 /host-tmp/fool-external-focused-final.log; exit $status'`
  - Tests run: 18, failures: 0, errors: 0, skipped: 0.
- Green module check:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -v /tmp:/host-tmp -w /workspace maven:3.9-eclipse-temurin-17 sh -lc 'mvn -pl fool-view -am -DfailIfNoTests=false test > /host-tmp/fool-external-module.log 2>&1; status=$?; tail -n 80 /host-tmp/fool-external-module.log; exit $status'`
  - Tests run: 100, failures: 0, errors: 0, skipped: 0.
- `git diff --check`
  - Passed.
- `python scripts/check_repo_harness.py`
  - Repository harness validation passed.

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
- No Docker seed route currently invokes an external-model operation; the
  executable behavior is covered by the focused unit test.

## Remaining Risk

- Richer nested external-model edge cases, WCF/JSON operation types, operation
  triggers, and routed-connection transaction behavior remain future work.
