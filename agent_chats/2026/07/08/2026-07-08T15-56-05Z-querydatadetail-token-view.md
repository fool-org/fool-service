# QueryDataDetail Token View Lookup

## Prompt

Continue the FoolFrame migration while keeping View rendering/data lookup on
protocol boundaries instead of concrete business DTO shortcuts.

## Scope

- Added a token-aware `DataQueryService.queryLegacyViewDataDetail(...)`
  overload.
- Updated `DataController.queryDataDetail` to pass `request.getToken()`.
- Kept `fool-view` independent from `fool-auth`; this only preserves the token
  at the View lookup boundary.

## Changed Files

- `fool-view/src/main/java/org/fool/framework/view/api/DataController.java`
- `fool-view/src/main/java/org/fool/framework/view/service/DataQueryService.java`
- `fool-view/src/test/java/org/fool/framework/view/api/DataControllerLegacyQueryDataDetailTest.java`
- `fool-view/src/test/java/org/fool/framework/view/service/DataQueryServiceDetailTest.java`
- `docs/migration/foolframe-parity.md`
- `tasks.md`

## Validation

- Red: `docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-view -am -Dtest=DataControllerLegacyQueryDataDetailTest,DataQueryServiceDetailTest test` failed because the token-aware overload did not exist.
- Green: same focused test after adding the overload and controller pass-through.

## Skipped Checks

- Full `fool-view` module tests were not rerun for this controller/service
  boundary-only change.

## Risks

- `ViewDataService` currently does not consume token internally, so this is a
  protocol boundary fix rather than full auth/context `IdExp` support.
