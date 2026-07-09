# Trigger External Model Command Parity

## Prompt

Continue the Docker/Vue/FoolFrame migration, keep changes atomic, maximize
reuse, avoid business DTO binding, and control file size.

## Scope

- Compared trigger external-model behavior with the existing
  `DataQueryService` `runoperation` command path and FoolFrame
  `ModelMethodContext.ExcuteOperation`.
- Migrated the focused model-trigger `ExuteOutModelMethod` update/map slice
  into `ModelDataService`.
- Left richer external-model create/delete/detail-fallback trigger edge cases
  and routed-connection transaction parity in the remaining migration list.

## Changes

- Added legacy `SW_SYS_OPERATION` mapping to the runtime `Operation` model.
- `ModelDataService#getModel` now hydrates runtime model operations and
  operation commands from `SW_SYS_OPERATION` / `SW_SYS_COMMANDS`.
- `ModelDataService` trigger command execution now handles
  `EXUTE_OUT_MODEL_METHOD` by resolving the target id through the shared
  `OperationCommandValueResolver`, applying the target operation commands with
  the source row as value source, and reusing public target persistence.
- Added
  `ModelDataServiceTest#saveDataExecutesLegacyTriggerOutModelCommand`.
- Updated `tasks.md` and `docs/migration/foolframe-parity.md`.

## Validation

- Red test before implementation:
  `ModelDataServiceTest#saveDataExecutesLegacyTriggerOutModelCommand` failed
  with `expected:<[closed]> but was:<[pending]>`.
- Focused green:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-model -am -Dtest=ModelDataServiceTest#saveDataExecutesLegacyTriggerOutModelCommand -DfailIfNoTests=false test`
  passed.
- Module gate:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-model -am -DfailIfNoTests=false test`
  passed, 76 tests.
- `python scripts/check_repo_harness.py` passed.
- `git diff --check` passed.

## Runtime Evidence

- `docker compose ps` showed backend and frontend running, with MySQL and
  Redis healthy.

## Risks

- Trigger external-model command coverage is currently proven for the
  update/map slice. Create/delete/detail-fallback trigger cases are still
  broader migration edge cases.
- Routed-connection transaction behavior remains open.
