# Prompt

Continue the Docker/FoolFrame/Vue migration, keep changes small, maximize
reuse, and avoid binding frontend/backend behavior to business DTO shortcuts.

# Scope

- `fool-view` legacy `saveobj` / `savenewobj` dynamic data construction when
  model metadata has no explicit id property.
- Migration status and task-state evidence for this slice.

# Changes

- Added a focused unit test proving a legacy `saveobj` request id is written
  into `SYSID` when the target model has no explicit id property.
- Reused a shared `setLegacyId` helper for top-level save objects and child
  item rows, preserving explicit id-property behavior and falling back to
  `SYSID` only when metadata omits the id property.
- Updated `docs/migration/foolframe-parity.md` and `tasks.md`.

# Validation

- Red:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -Dtest=DataQueryServiceSaveObjTest#saveLegacyObjectWritesSysIdWhenModelHasNoIdProperty test`
  failed with `expected:<6101> but was:<null>`.
- Green:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -Dtest=DataQueryServiceSaveObjTest#saveLegacyObjectWritesSysIdWhenModelHasNoIdProperty test`
  passed: 1 test.
- Green:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am test`
  passed: 161 tests.
- Green:
  `python scripts/check_repo_harness.py`.
  passed.

# Runtime Evidence

- No Docker browser smoke was added in this slice; the change is a shared save
  data construction behavior covered by focused and dependent module tests.

# Risks

- This does not complete remaining richer `SCPB05-Soway.Model` collection,
  external-model, or routed-transaction parity.

# Follow-ups

- Continue remaining migration work from
  `docs/migration/foolframe-parity.md`.
