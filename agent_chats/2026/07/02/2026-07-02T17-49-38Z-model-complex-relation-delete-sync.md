# Agent Chat: Model complex relation delete sync

## Prompt

- Continue the active migration goal: Docker runtime, parity against `../FoolFrame`, Vue frontend, and timely atomic commits.

## Scope

- Migrate a narrow `SCPB05-Soway.Model` runtime mutation slice: dynamic save now deletes legacy `Many2Many` and `Recurve` relation-table rows listed in `SubItemList.deleteList`.
- Reuse the existing relation column direction used for relation-table inserts.

## Changed Files

- `fool-model/src/main/java/org/fool/framework/model/service/ModelDataService.java`
- `fool-model/src/test/java/org/fool/framework/model/service/ModelDataServiceTest.java`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/02/2026-07-02T17-49-38Z-model-complex-relation-delete-sync.md`

## Validation

- RED: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-model -am -Dtest=ModelDataServiceTest#saveDataDeletesLegacyManyToManyRelationRows+saveDataDeletesLegacyRecurveRelationRows test`
  - Failed as expected: both relation row-count assertions expected `0` and got `1`.
- GREEN focused: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-model -am -Dtest=ModelDataServiceTest#saveDataDeletesLegacyManyToManyRelationRows+saveDataDeletesLegacyRecurveRelationRows test`
  - Passed with `Tests run: 2, Failures: 0, Errors: 0, Skipped: 0`.
- Module: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-model -am test`
  - Passed. Reactor summary through `fool-model` was `BUILD SUCCESS`; result included `Tests run: 56, Failures: 0, Errors: 0, Skipped: 0`.

## Skipped Checks

- Full root `mvn test` was not rerun for this narrow `fool-model` mutation slice.
- Frontend checks were not rerun because no frontend files changed.

## Risks And Follow-ups

- This covers relation-table delete-list sync for `Many2Many` and `Recurve`.
- Remaining model mutation parity still includes richer collection state/load-type parity, trigger side effects, old-ID updates, and routed-connection transaction behavior.
