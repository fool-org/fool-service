# Operation Trigger Status Correction

## Prompt

Continue the FoolFrame migration while keeping Vue view-first/data-second flow
and avoiding business DTO coupling.

## Scope

- Rechecked legacy `runoperation` model-trigger side effects against
  `../FoolFrame` `ModelMethodContext` and `SqlServer.dbContext`.
- Confirmed migrated `runoperation` already calls public
  `ModelDataService.createData`, `saveData`, and `deleteData`, which run normal
  model triggers.
- Removed the stale "operation-trigger side-effect execution" remaining-work
  wording. No production code changed in this slice.

## Changed Files

- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/09/2026-07-09T07-45-02Z-operation-trigger-status.md`

## Validation

- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -DfailIfNoTests=false -Dtest=DataQueryServiceRunOperationTest#runLegacyUpdateOperationSavesObjectAndReturnsSuccessMessage test`
  - Passed: BUILD SUCCESS, Tests run: 1, Failures: 0.
- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-model -am -DfailIfNoTests=false -Dtest=ModelDataServiceTest#saveDataExecutesLegacySaveTriggerSetValue -Dspring.datasource.url="jdbc:mysql://mysql:3306/car_wash?serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8&allowMultiQueries=true&autoReconnect=true" -Dspring.datasource.username=root -Dspring.datasource.password=Pa88word test`
  - Passed: BUILD SUCCESS, Tests run: 1, Failures: 0.

## Runtime Evidence

- Docker runtime was already up for this migration line.
- No Docker rebuild was needed because this slice only corrects parity status
  documentation after source/test verification.

## Risks

- This does not complete WCF/JSON/external-model edge cases.
- This does not complete routed-connection transaction behavior.
