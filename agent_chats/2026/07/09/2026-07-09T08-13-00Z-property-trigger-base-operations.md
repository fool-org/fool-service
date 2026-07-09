# Property Trigger Base Operation Parity

## Prompt

Continue the Docker/Vue/FoolFrame migration, keep changes atomic, maximize
reuse, and keep protocol/rendering work aligned with the real legacy flow.

## Scope

- Compared non-assembly trigger base-operation behavior with
  `../FoolFrame/src/Server/SCPB05-Soway.Model/ModelMethodContext.cs`.
- Migrated property-trigger `Create` / `Update` / `Delete` base-operation
  execution by reusing the existing model-trigger persistence path.
- Kept routed-connection transaction behavior and richer edge cases open.

## Changes

- `ModelDataService` now has one shared private trigger base-operation executor
  for model, property, and collection triggers.
- Property `SET` and collection `ItemsAdd` / `ItemsDelete` triggers can now run
  `CREATE`, `UPDATE`, `DELETE`, and `ASSEBMLY` after their commands.
- Added `ModelDataServiceTest#saveDataExecutesLegacyPropertySetTriggerDeleteOperation`
  to prove a metadata-backed property `SET` trigger deletes the current row.
- Updated `tasks.md` and `docs/migration/foolframe-parity.md`.

## Validation

- Red test before implementation:
  `ModelDataServiceTest#saveDataExecutesLegacyPropertySetTriggerDeleteOperation`
  failed with `expected:<0> but was:<1>`.
- Focused green:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-model -am -Dtest=ModelDataServiceTest#saveDataExecutesLegacyPropertySetTriggerDeleteOperation -DfailIfNoTests=false test`
  passed.
- Module gate:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-model -am -DfailIfNoTests=false test`
  passed, 75 tests.
- `python scripts/check_repo_harness.py` passed.
- `git diff --check` passed.

## Runtime Evidence

- `docker compose ps` showed backend and frontend running, with MySQL and Redis
  healthy.

## Risks

- Trigger operation execution still uses the current default `ModelDataService`
  persistence route; routed-connection transaction parity remains open.
- Complex property-trigger create/update/delete edge cases beyond the focused
  delete proof remain covered by the broader runtime edge-case backlog.

## Follow-ups

- Continue remaining `SCPB05-Soway.Model` work on external-model command
  recursion, collection state edge cases, and routed-connection transaction
  behavior.
