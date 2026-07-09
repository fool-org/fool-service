# Prompt

Continue the Docker/FoolFrame/Vue migration, keep changes reusable and small,
and fix the next legacy parity gap without binding Vue/data flow to business
DTOs.

# Scope

- `fool-model` dynamic data reads for parent models that do not declare an
  explicit id property and therefore rely on FoolFrame `SYSID`.
- Owned collection hydration after `getOneData` / list queries.
- Migration status and task-state evidence for this slice.

# Changes

- Added a regression test where a dynamic parent model has no id property,
  stores its key in `SYSID`, and owns child rows through a One2Many relation.
- Kept `SqlGenerator` generic: it only selects the properties passed by its
  caller.
- Added a small `ModelDataService` query helper that appends a synthetic
  `SYSID` read property only for no-idProperty model data reads.
- Mapped selected `SYSID` into `DbMysqlDynamic` as the fallback id value.
- Reused the existing fallback id path when collecting parent ids and attaching
  owned collection rows.
- Updated `docs/migration/foolframe-parity.md` and `tasks.md`.

# Validation

- Red:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-model -am -Dtest=ModelDataServiceTest#getOneDataLoadsLegacyCollectionsBySysIdWhenModelHasNoIdProperty test`
  failed because `data.get("SYSID")` was `null`.
- Green:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-model -am -Dtest=ModelDataServiceTest#getOneDataLoadsLegacyCollectionsBySysIdWhenModelHasNoIdProperty test`
  passed: 1 test.
- Green:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-model -am test`
  passed: 86 tests.
- Green:
  `python scripts/check_repo_harness.py`
  passed.

# Runtime Evidence

- The focused test logged the parent read as `SELECT ORDER_NAME,SYSID ...` and
  the child read with `ORDER_SYSID AS __parent_id`, proving the parent `SYSID`
  value is available for collection bucketing.

# Risks

- This does not complete every remaining `SCPB05-Soway.Model` parity edge; it
  closes the no-idProperty owned collection read path.

# Follow-ups

- Continue the remaining richer collection state, external-model, routed
  transaction, and `car_wash` schema work tracked in
  `docs/migration/foolframe-parity.md`.
