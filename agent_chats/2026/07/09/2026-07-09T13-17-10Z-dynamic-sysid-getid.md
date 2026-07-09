# Prompt

Continue the Docker/FoolFrame/Vue migration, keep changes small, and maximize
reuse while closing real legacy parity gaps.

# Scope

- `fool-model` dynamic row id behavior when model metadata has no explicit id
  property.
- Migration status and task-state evidence for this slice.

# Changes

- Added a focused unit test for `DbMysqlDynamic.getId()` on a model without an
  id property but with a legacy `SYSID` value.
- Made `DbMysqlDynamic.getId()` return `SYSID` in that metadata shape.
- Updated `docs/migration/foolframe-parity.md` and `tasks.md`.

# Validation

- Red:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-model -am -Dtest=DbMysqlDynamicTest#getIdFallsBackToLegacySysIdWhenModelHasNoIdProperty test`
  failed with `expected:<6101> but was:<null>`.
- Green:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-model -am -Dtest=DbMysqlDynamicTest#getIdFallsBackToLegacySysIdWhenModelHasNoIdProperty test`
  passed: 1 test.
- Green:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-model -am test`
  passed: 87 tests.
- Green:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am test`
  passed: 160 tests.
- Green:
  `python scripts/check_repo_harness.py`
  passed.

# Runtime Evidence

- No Docker browser smoke was added; this is a shared model-object behavior
  slice covered by focused and dependent module tests.

# Risks

- This does not complete remaining richer `SCPB05-Soway.Model` collection,
  external-model, or routed-transaction parity.

# Follow-ups

- Continue remaining migration work from
  `docs/migration/foolframe-parity.md`.
