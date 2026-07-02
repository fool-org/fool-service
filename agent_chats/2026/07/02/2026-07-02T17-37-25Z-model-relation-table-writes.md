# Agent Chat: Model relation-table writes

## Prompt

- Continue the active migration goal: Docker runtime, parity against `../FoolFrame`, Vue frontend, and timely atomic commits.
- User asked for current migration percentage while this slice was in progress.

## Scope

- Migrate a narrow `SCPB05-Soway.Model` runtime mutation slice: after dynamic create/save succeeds, write legacy `Many2Many` and `Recurve` relation rows into the configured relation table.
- Keep the implementation limited to relation-table inserts for existing child items.

## Changed Files

- `fool-model/src/main/java/org/fool/framework/model/service/ModelDataService.java`
- `fool-model/src/test/java/org/fool/framework/model/service/ModelDataServiceTest.java`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/02/2026-07-02T17-37-25Z-model-relation-table-writes.md`

## Validation

- RED: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-model -am -Dtest=ModelDataServiceTest#createDataWritesLegacyManyToManyRelationRows+saveDataWritesLegacyManyToManyRelationRows test`
  - Failed as expected: both relation-table row-count assertions expected `1` and got `0`.
- GREEN focused: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-model -am -Dtest=ModelDataServiceTest#createDataWritesLegacyManyToManyRelationRows+saveDataWritesLegacyManyToManyRelationRows+createDataWritesLegacyRecurveRelationRows test`
  - Passed with `Tests run: 3, Failures: 0, Errors: 0, Skipped: 0`.
- Module: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-model -am test`
  - Passed. Reactor summary through `fool-model` was `BUILD SUCCESS`; result included `Tests run: 52, Failures: 0, Errors: 0, Skipped: 0`.

## Skipped Checks

- Full root `mvn test` was not rerun for this narrow `fool-model` mutation slice.
- Frontend checks were not rerun because no frontend files changed.

## Risks And Follow-ups

- This covers relation-table inserts for existing child `IDynamicData` items.
- It does not yet create missing child rows, delete removed relation rows, delete child rows, fire operation/model/property triggers, handle old-ID updates, or reproduce routed-connection transaction behavior.
