# Report Items Row Projection

## Prompt

Continue the Docker/FoolFrame/Vue migration, keeping View rendering and data
loading metadata-driven instead of binding to concrete business DTOs.

## Scope

- Removed the report-grid adapter's `ListDataItem.values` row-map fallback.
- Added a focused controller test proving DTO-only map values do not leak into
  `makereport` output when legacy row `Items` drive the rendered cells.
- Updated the migration parity log and task board for the completed slice.

## Changed Files

- `fool-view/src/main/java/org/fool/framework/view/api/ReportController.java`
- `fool-view/src/test/java/org/fool/framework/view/api/ReportControllerTest.java`
- `docs/migration/foolframe-parity.md`
- `tasks.md`

## Validation

- `docker run --rm -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -Dtest=ReportControllerTest -DfailIfNoTests=false -DfailIfNoSpecifiedTests=false test`
  - Result: passed, `ReportControllerTest` ran 16 tests with 0 failures and 0 errors.
- `python scripts/check_repo_harness.py`
  - Result: passed.
- `git diff --check`
  - Result: passed.

## Risks

- The broader FoolFrame report engine still has open parity work around saved
  report execution/export and table source adapters; this slice only removes a
  DTO-map fallback from the migrated flat-grid REST path.
