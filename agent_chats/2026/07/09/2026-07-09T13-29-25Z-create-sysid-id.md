# Prompt

Continue the Docker/FoolFrame/Vue migration, keep changes small, maximize
reuse, and keep protocol/data paths View-first instead of business DTO-bound.

# Scope

- `fool-model` dynamic create behavior for models without explicit id metadata.
- Migration status and task-state evidence for this slice.

# Changes

- Added a focused test proving `createData` writes `SYSID` when a dynamic model
  has no explicit id property and the row carries a legacy id.
- Added the minimal `createData` fallback column write for no-idProperty
  models, reusing the existing dynamic id fallback.
- Updated `docs/migration/foolframe-parity.md` and `tasks.md`.

# Validation

- Red:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-model -am -Dtest=ModelDataServiceSysIdMutationTest#createDataWritesSysIdWhenModelHasNoIdProperty test`
  failed with `SQL [INSERT INTO runtime_create_sysid_object (OBJECT_NAME) VALUES (?)]; Field 'SYSID' doesn't have a default value`.
- Green:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-model -am -Dtest=ModelDataServiceSysIdMutationTest#createDataWritesSysIdWhenModelHasNoIdProperty test`
  passed.
- Green:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-model -am test`
  passed: 88 tests.
- Green:
  `python scripts/check_repo_harness.py`.
  passed.

# Runtime Evidence

- No browser smoke was added; this is shared model create behavior covered by
  focused and dependent module tests.

# Risks

- This does not complete the remaining richer collection, external-model, or
  routed-transaction parity listed in the migration document.

# Follow-ups

- Continue remaining migration work from
  `docs/migration/foolframe-parity.md`.
