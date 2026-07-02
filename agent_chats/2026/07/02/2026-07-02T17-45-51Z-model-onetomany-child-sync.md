# Agent Chat: Model One2Many child-row sync

## Prompt

- Continue the active migration goal: Docker runtime, parity against `../FoolFrame`, Vue frontend, and timely atomic commits.

## Scope

- Migrate a narrow `SCPB05-Soway.Model` runtime mutation slice: dynamic parent create/save now syncs legacy `One2Many` child rows by writing the parent ID to the relation target column.
- Use existing `SubItemList.deleteList` as the Java-side equivalent for legacy removed child items.

## Changed Files

- `fool-model/src/main/java/org/fool/framework/model/service/ModelDataService.java`
- `fool-model/src/test/java/org/fool/framework/model/service/ModelDataServiceTest.java`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/02/2026-07-02T17-45-51Z-model-onetomany-child-sync.md`

## Validation

- RED: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-model -am -Dtest=ModelDataServiceTest#createDataWritesLegacyOneToManyChildRows+saveDataSyncsLegacyOneToManyChildRows test`
  - Failed as expected: create left child row count at `0`; save left old rows unchanged.
- GREEN focused: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-model -am -Dtest=ModelDataServiceTest#createDataWritesLegacyOneToManyChildRows+saveDataSyncsLegacyOneToManyChildRows test`
  - Passed with `Tests run: 2, Failures: 0, Errors: 0, Skipped: 0`.
- Module: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-model -am test`
  - Passed. Reactor summary through `fool-model` was `BUILD SUCCESS`; result included `Tests run: 54, Failures: 0, Errors: 0, Skipped: 0`.

## Skipped Checks

- Full root `mvn test` was not rerun for this narrow `fool-model` mutation slice.
- Frontend checks were not rerun because no frontend files changed.

## Risks And Follow-ups

- This covers `One2Many` child create/update and delete-list sync through `SubItemList`.
- Remaining model mutation parity still includes richer collection state/load-type parity, Many2Many/Recurve relation-table delete sync, trigger side effects, old-ID updates, and routed-connection transaction behavior.
