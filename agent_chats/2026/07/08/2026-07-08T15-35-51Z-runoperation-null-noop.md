# Runoperation NULL No-Op

## Prompt

Continue the FoolFrame migration while keeping protocol behavior aligned with
legacy source and avoiding unnecessary implementation.

## Scope

- Checked FoolFrame `ModelMethodContext.ExcuteOperation` and
  `HandlerRunOperation`.
- Matched legacy `OperationBaseType.NULL` behavior: command evaluation may run,
  but no create/update/delete persistence is performed, and the handler returns
  operation success.
- Left JSON/WCF operation types unsupported; there is no migrated handler to
  call yet.

## Changed Files

- `fool-view/src/main/java/org/fool/framework/view/service/DataQueryService.java`
- `fool-view/src/test/java/org/fool/framework/view/service/DataQueryServiceRunOperationTest.java`
- `docs/migration/foolframe-parity.md`
- `tasks.md`

## Validation

- Red: `docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-view -am -Dtest=DataQueryServiceRunOperationTest#runLegacyNullOperationReturnsSuccessWithoutPersistence test` failed because NULL returned an unsuccessful empty result.
- Green: same focused test after adding the NULL branch.
- Green: `docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-view -am -Dtest=DataQueryServiceRunOperationTest test`.

## Skipped Checks

- Full `fool-view` module tests were not rerun for this one-branch isolated
  service behavior after the focused runoperation class passed.

## Risks

- NULL operations with command-side effects only mutate the loaded dynamic
  object in memory unless a later command path explicitly persists; this matches
  the audited FoolFrame switch behavior.
