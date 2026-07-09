# Prompt

Continue the Docker/FoolFrame/Vue migration, keep reuse high, control file
size, and avoid binding rendered View pages to concrete business DTOs.

# Scope

- `fool-view` View/data protocol id handling for dynamic rows without an
  explicit model id property.
- Migration status and task-state evidence for the completed slice.

# Changes

- Added `LegacyDynamicIds.id(...)` as the shared View-layer dynamic id helper.
- Routed list row ids, detail `ObjId`, child collection `DataID`,
  BusinessObject value ids, `inputquery` candidate ids, and blank-detail
  first-row lookup through the helper.
- Extended existing View adapter, detail, and inputquery tests to prove
  `SYSID` fallback when `IDynamicData.getId()` is null.
- Updated `docs/migration/foolframe-parity.md` and `tasks.md`.

# Validation

- Red:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -Dtest=ViewDataAdapterTest,DataQueryServiceInputQueryTest test`
  failed with six expected null/blank id assertions.
- Red:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -Dtest=DataQueryServiceDetailTest#queryLegacyViewDataDetailUsesFirstResultWhenObjectIdAndIdExpressionAreBlank test`
  failed because the blank-detail first-row lookup returned null.
- Green:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -Dtest=ViewDataAdapterTest,DataQueryServiceInputQueryTest,DataQueryServiceDetailTest test`
  passed: 28 tests.
- Green:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am test`
  passed: 158 tests.
- Green:
  `python scripts/check_repo_harness.py` passed.

# Runtime Evidence

- Not rerun in Docker runtime doctor; this slice is covered by focused
  `fool-view` unit tests around the affected protocol outputs.

# Risks

- This keeps the same narrow `getId()` then `SYSID` fallback already used by
  model mutation parity. Other legacy key aliases remain unimplemented unless
  a concrete migrated surface needs them.

# Follow-ups

- Continue remaining `SCPB05-Soway.Model` edge cases and complete `car_wash`
  schema/migration coverage from `docs/migration/foolframe-parity.md`.
