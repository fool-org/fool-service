# Agent Chat: Model saveDataList simple dynamic rows

## Prompt

- Continue the active migration goal: Docker runtime, parity against `../FoolFrame`, Vue frontend, and timely atomic commits.

## Scope

- Migrate a narrow `SCPB05-Soway.Model` runtime mutation slice: simple batch saves for dynamic rows.
- The implementation delegates to the existing simple `saveData` path for each row.

## Changed Files

- `fool-model/src/main/java/org/fool/framework/model/service/ModelDataService.java`
- `fool-model/src/test/java/org/fool/framework/model/service/ModelDataServiceTest.java`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/02/2026-07-02T17-09-02Z-model-save-data-list.md`

## Validation

- RED: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-model -am -Dtest=ModelDataServiceTest#saveDataListUpdatesLegacySimpleDynamicRows test`
  - Failed as expected: rows still contained `Before save 1` and `Before save 2`.
- GREEN focused: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -DfailIfNoTests=false -pl fool-model -am -Dtest=ModelDataServiceTest#saveDataListUpdatesLegacySimpleDynamicRows test`
  - Passed.
- Module: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-model -am test`
  - Passed. Reactor summary through `fool-model` was `BUILD SUCCESS`; module result included `Tests run: 45, Failures: 0, Errors: 0, Skipped: 0`.

## Skipped Checks

- Full root `mvn test` was not rerun for this narrow `fool-model` service slice.
- Frontend checks were not rerun because no frontend files changed.

## Risks And Follow-ups

- `ModelDataService#saveDataList` is non-transactional and only covers rows supported by `saveData`.
- Remaining mutation parity includes relation/collection writes/deletes, DBMaps writes, trigger side effects, old-id updates, and routed-connection transactions.
