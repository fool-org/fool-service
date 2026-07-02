# Agent Chat: Model DBMaps create/update writes

## Prompt

- Continue the active migration goal: Docker runtime, parity against `../FoolFrame`, Vue frontend, and timely atomic commits.

## Scope

- Migrate a narrow `SCPB05-Soway.Model` runtime mutation slice: DBMaps-backed business-object values are flattened into owner table columns during `createData` and `saveData`.
- Share the simple and DBMaps column expansion path for create/update.

## Changed Files

- `fool-model/src/main/java/org/fool/framework/model/service/ModelDataService.java`
- `fool-model/src/test/java/org/fool/framework/model/service/ModelDataServiceTest.java`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/02/2026-07-02T17-30-22Z-model-dbmaps-writes.md`

## Validation

- RED: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-model -am -Dtest=ModelDataServiceTest#createDataWritesLegacyMultiDbMapColumns+saveDataUpdatesLegacyMultiDbMapColumnsById test`
  - Failed as expected: create left `CUSTOMER_ID` null, and save returned `false`.
- GREEN focused: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-model -am -Dtest=ModelDataServiceTest#createDataWritesLegacyMultiDbMapColumns+saveDataUpdatesLegacyMultiDbMapColumnsById test`
  - Passed.
- Module: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-model -am test`
  - Passed. Reactor summary through `fool-model` was `BUILD SUCCESS`; result included `Tests run: 49, Failures: 0, Errors: 0, Skipped: 0`.

## Skipped Checks

- Full root `mvn test` was not rerun for this narrow `fool-model` mutation slice.
- Frontend checks were not rerun because no frontend files changed.

## Risks And Follow-ups

- DBMaps write support covers nested `IDynamicData` values for simple create/update rows.
- Remaining model mutation parity still includes relation/collection writes/deletes, trigger side effects, old-id updates, and routed-connection transactions.
