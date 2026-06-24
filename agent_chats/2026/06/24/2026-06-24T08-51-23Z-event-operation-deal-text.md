# Event Operation Deal Text Parity

## Prompt

- Continue the `/goal`: keep Docker running, migrate against `../FoolFrame`, keep the frontend on Vue, and commit atomically.

## Scope

- Compared `../FoolFrame/src/Server/SCPB09-SOWAY.EVENT/EventFactory.cs`.
- Migrated the legacy event creation behavior where `EVT_DEALMSG` is populated from `defs.Operation.Name`.
- Added a Java `EventDefinition.operationName` surface for resolved operation display text.
- Updated `EventRuntimeService` to prefer `operationName` for `EventRecord.dealOperationText` and fall back to `operationId` when a display name is not available.

## Changed Files

- `fool-event/src/main/java/org/fool/framework/event/EventDefinition.java`
- `fool-event/src/main/java/org/fool/framework/event/EventRuntimeService.java`
- `fool-event/src/test/java/org/fool/framework/event/EventMigrationTest.java`
- `docs/migration/foolframe-parity.md`

## TDD Evidence

- Red test:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -q -pl fool-event -am -Dtest=EventMigrationTest#eventRuntimeUsesLegacyOperationNameForDealText -DfailIfNoTests=false test`
  - Result: test compilation failed because `EventDefinition#setOperationName(String)` did not exist.
- Green focused test:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -q -pl fool-event -am -Dtest=EventMigrationTest#eventRuntimeUsesLegacyOperationNameForDealText -DfailIfNoTests=false test`
  - Result: passed.
- Event migration regression:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -q -pl fool-event -am -Dtest=EventMigrationTest -DfailIfNoTests=false test`
  - Result: `EventMigrationTest`: passed.

## Runtime / Skips

- No frontend behavior changed in this slice.
- Repository harness:
  - `python scripts/check_repo_harness.py`
  - Result: passed.
- Whitespace check:
  - `git diff --check`
  - Result: passed.
- Docker rebuild was already run immediately before this slice; this commit only changes event module code and focused tests.

## Follow-ups

- Wire persisted operation display-name loading when the event definition repository grows beyond the current `EVTDEF_OPERATION` id surface.
- Continue fuller Event dynamic object-query parity and broader FoolFrame checklist work.
