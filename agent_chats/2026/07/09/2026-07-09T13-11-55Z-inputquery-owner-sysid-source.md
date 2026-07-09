# Prompt

Continue the Docker/FoolFrame/Vue migration, keep View-first data flow, and
avoid concrete business DTO binding.

# Scope

- `fool-view` legacy `inputquery` source-list lookup when the owner/current
  model has no explicit id property and therefore relies on FoolFrame `SYSID`.
- Migration status and task-state evidence for this slice.

# Changes

- Added a focused regression test for an added child-row lookup using
  `#.availableCustomers` where the owner model has no id property.
- Reused a small `DataQueryService` id-column helper so source-list owner and
  current-object lookup filters use the model id column when present and
  `SYSID` otherwise.
- Updated `docs/migration/foolframe-parity.md` and `tasks.md`.

# Validation

- Red:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -Dtest=DataQueryServiceInputQueryTest#inputQueryFiltersAddedItemSourceListFromOwnerSysIdWhenOwnerHasNoIdProperty test`
  failed with `expected:<2> but was:<0>`.
- Green:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -Dtest=DataQueryServiceInputQueryTest#inputQueryFiltersAddedItemSourceListFromOwnerSysIdWhenOwnerHasNoIdProperty test`
  passed: 1 test.
- Green:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am test`
  passed: 160 tests.
- Green:
  `python scripts/check_repo_harness.py`
  passed.

# Runtime Evidence

- The focused test verifies the source owner query filter is `` `SYSID`= ? ``
  with the request `OwnerId`, then renders candidate ids through the shared
  legacy dynamic id helper.

# Risks

- This is unit/service coverage for the protocol branch; no Docker browser
  runtime smoke was added for this narrow lookup edge.

# Follow-ups

- Continue remaining `SCPB05-Soway.Model` edge cases and complete `car_wash`
  schema/migration coverage from `docs/migration/foolframe-parity.md`.
