# Prompt

Continue the Docker/FoolFrame/Vue migration, keep View-first data flow, and
avoid concrete business DTO binding.

# Scope

- `fool-view` legacy `inputquery` when the BusinessObject target model has no
  explicit id property.
- Migration status and task-state evidence for the completed slice.

# Changes

- Added a focused regression test for `inputquery` target models with a show
  property but no id property.
- Replaced `List.of(idProperty, showProperty)` with a tiny helper that filters
  null properties and avoids duplicate id/show properties.
- Kept candidate id output on the existing `LegacyDynamicIds.id(...)` path.
- Updated `docs/migration/foolframe-parity.md` and `tasks.md`.

# Validation

- Red:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -Dtest=DataQueryServiceInputQueryTest#inputQueryAllowsTargetModelWithoutExplicitIdProperty test`
  failed with a `NullPointerException`.
- Green:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -Dtest=DataQueryServiceInputQueryTest#inputQueryAllowsTargetModelWithoutExplicitIdProperty test`
  passed.
- Green:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am test`
  passed: 159 tests.
- Green:
  `python scripts/check_repo_harness.py` passed.

# Runtime Evidence

- Runtime doctor not rerun for this narrow unit-covered `inputquery` service
  branch.

# Risks

- This does not synthesize `SYSID` into SQL projection; it only keeps the
  service query property list valid when model metadata lacks an explicit id
  property.

# Follow-ups

- Continue remaining `SCPB05-Soway.Model` edge cases and complete `car_wash`
  schema/migration coverage from `docs/migration/foolframe-parity.md`.
