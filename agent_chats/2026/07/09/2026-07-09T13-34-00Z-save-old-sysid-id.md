# Prompt

Continue the Docker/FoolFrame/Vue migration, keep changes small, maximize
reuse, and keep protocol/data paths metadata-driven.

# Scope

- `fool-model` dynamic save behavior for no-idProperty models when `SYSID`
  changes.
- Migration status and task-state evidence for this slice.

# Changes

- Added a focused test proving `saveData` uses the old `SYSID` as the update
  key when a no-idProperty dynamic row changes `SYSID`.
- Reused the existing `DbMysqlDynamic` old-value map in `lookupIdValue` for the
  no-idProperty `SYSID` fallback path.
- Updated `docs/migration/foolframe-parity.md` and `tasks.md`.

# Validation

- Red:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-model -am -Dtest=ModelDataServiceSysIdMutationTest#saveDataUsesOldSysIdWhenModelHasNoIdProperty test`
  failed with `expected:<true> but was:<false>`.
- Green:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-model -am -Dtest=ModelDataServiceSysIdMutationTest#saveDataUsesOldSysIdWhenModelHasNoIdProperty test`
  passed.
- Green:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-model -am test`
  passed: 89 tests.
- Green:
  `python scripts/check_repo_harness.py`.
  passed.

# Runtime Evidence

- No browser smoke was added; this is shared model save behavior covered by
  focused and dependent module tests.

# Risks

- This does not complete the remaining richer collection, external-model, or
  routed-transaction parity listed in the migration document.

# Follow-ups

- Continue remaining migration work from
  `docs/migration/foolframe-parity.md`.
