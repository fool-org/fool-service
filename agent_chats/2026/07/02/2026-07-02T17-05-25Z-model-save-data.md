# Agent Chat: Model saveData simple dynamic row

## Prompt

- Continue the active migration goal: Docker runtime, parity against `../FoolFrame`, Vue frontend, and timely atomic commits.

## Scope

- Migrate a narrow `SCPB05-Soway.Model` runtime mutation slice: simple dynamic row update by model key column.
- Legacy reference checked: `../FoolFrame/src/Server/SCPB05-Soway.Model/SqlServer/dbContext.cs` `Save` updates non-key columns and filters by key column before relation and trigger handling.

## Changed Files

- `fool-model/src/main/java/org/fool/framework/model/service/ModelDataService.java`
- `fool-model/src/test/java/org/fool/framework/model/service/ModelDataServiceTest.java`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/02/2026-07-02T17-05-25Z-model-save-data.md`

## Validation

- RED: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-model -am -Dtest=ModelDataServiceTest#saveDataUpdatesLegacySimpleDynamicRowById test`
  - Failed as expected: expected `After save` but row still contained `Before save`.
- GREEN focused: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -DfailIfNoTests=false -pl fool-model -am -Dtest=ModelDataServiceTest#saveDataUpdatesLegacySimpleDynamicRowById test`
  - Passed.
- Module: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-model -am test`
  - Passed. Reactor summary through `fool-model` was `BUILD SUCCESS`; module result included `Tests run: 44, Failures: 0, Errors: 0, Skipped: 0`.

## Skipped Checks

- Full root `mvn test` was not rerun for this narrow `fool-model` service slice.
- Frontend checks were not rerun because no frontend files changed.

## Risks And Follow-ups

- `ModelDataService#saveData` only supports `DbMysqlDynamic` simple scalar non-key column updates.
- Remaining mutation parity includes batch saves, relation/collection writes/deletes, DBMaps writes, trigger side effects, old-id updates, and routed-connection transactions.
