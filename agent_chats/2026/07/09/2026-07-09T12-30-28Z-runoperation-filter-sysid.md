# Prompt

Continue the Docker/FoolFrame/Vue migration, keep commits atomic, maximize
reuse, and avoid broad rewrites.

# Scope

Close one concrete `SCPB05-Soway.Model` runoperation parity gap: FoolFrame
`CheckCommand` always binds the filter command to the current object key;
`DataQueryService` only did that when `data.getId()` was non-null.

# Changes

- Added a focused regression test for runoperation `Filter` commands on a
  model without an explicit id property.
- Updated `DataQueryService.checkFilterCommand` to fall back to `SYSID` and
  the row's `SYSID` value before applying the configured raw filter.
- Updated `tasks.md` and `docs/migration/foolframe-parity.md`.

# Validation

- Red:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -Dtest=DataQueryServiceRunOperationTest#runLegacyUpdateOperationFilterUsesSysIdWhenModelHasNoIdProperty test`
  failed with SQL missing the `SYSID` predicate.
- Green:
  same command passed with `Tests run: 1, Failures: 0, Errors: 0, Skipped: 0`.
- Final:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -Dtest=DataQueryServiceRunOperationTest test`
  passed with `Tests run: 23, Failures: 0, Errors: 0, Skipped: 0`.
- `python scripts/check_repo_harness.py` passed.

# Runtime Evidence

No Docker runtime route was changed. Existing Compose services were already up
while this was validated through the Java 17 Maven container.

# Risks

This only covers the runoperation view-service path. Broader context values,
business-object assembly argument conversion, and cross-routed transaction
behavior remain separate migration work.

# Follow-ups

- Continue `SCPB05-Soway.Model` runtime parity only for the remaining concrete
  collection/external-model/routed-transaction gaps.
