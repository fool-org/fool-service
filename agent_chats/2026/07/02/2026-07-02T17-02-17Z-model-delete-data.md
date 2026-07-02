# Agent Chat: Model deleteData simple dynamic row

## Prompt

- Continue the active migration goal: Docker runtime, parity against `../FoolFrame`, Vue frontend, and timely atomic commits.

## Scope

- Migrate a narrow `SCPB05-Soway.Model` runtime mutation slice: simple dynamic row deletion by model key column.
- Legacy reference checked: `../FoolFrame/src/Server/SCPB05-Soway.Model/SqlServer/DynamicContext.cs` delegates `Delete` to `dbContext.Delete`, and `dbContext.delete` deletes by `DataTableName` plus key column.

## Changed Files

- `fool-model/src/main/java/org/fool/framework/model/service/ModelDataService.java`
- `fool-model/src/test/java/org/fool/framework/model/service/ModelDataServiceTest.java`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/02/2026-07-02T17-02-17Z-model-delete-data.md`

## Validation

- RED: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-model -am -Dtest=ModelDataServiceTest#deleteDataDeletesLegacySimpleDynamicRowById test`
  - Failed as expected with count `1` after delete.
- GREEN focused: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -DfailIfNoTests=false -pl fool-model -am -Dtest=ModelDataServiceTest#deleteDataDeletesLegacySimpleDynamicRowById test`
  - Passed.
- Module: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-model -am test`
  - Passed. Reactor summary through `fool-model` was `BUILD SUCCESS`; module result included `Tests run: 43, Failures: 0, Errors: 0, Skipped: 0`.

## Skipped Checks

- Full root `mvn test` was not rerun for this narrow `fool-model` service slice.
- Frontend checks were not rerun because no frontend files changed.

## Risks And Follow-ups

- `ModelDataService#deleteData` only supports `DbMysqlDynamic` simple key-column deletion.
- Remaining mutation parity includes save/update, batch saves, relation/collection writes/deletes, DBMaps writes, trigger side effects, and routed-connection transactions.
