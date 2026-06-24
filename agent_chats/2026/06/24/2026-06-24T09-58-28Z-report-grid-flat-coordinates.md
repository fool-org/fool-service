# Report grid flat-row coordinate migration

## Prompt

- Continue the active Docker/Vue/FoolFrame migration goal after the SimpleBoolExpression display-string slice.

## Scope

- Compared the current Java report grid renderer with the FoolFrame report matrix/cell coordinate model.
- Tightened `ReportGridRenderer` so flat query rows render as a two-dimensional grid: header cells keep their column index, and row values are emitted at the matching source column index.
- Kept paging metadata and row-major cell ordering unchanged.
- Updated FoolFrame parity notes for this specific grid rendering behavior.

## Changed Files

- `fool-report/src/main/java/org/fool/framework/report/ReportGridRenderer.java`
- `fool-report/src/test/java/org/fool/framework/report/ReportMigrationTest.java`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/06/24/2026-06-24T09-58-28Z-report-grid-flat-coordinates.md`

## Validation

- Red: `docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-report -am -DfailIfNoTests=false -Dtest=ReportMigrationTest#reportGridRendererBuildsLegacyMakeReportCellsFromFlatRows,ReportMigrationTest#reportGridRendererKeepsHeadersAndPagingForEmptyRows test`
  - Failed as expected because the second rendered column still had `col=0`.
- Green: `docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-report -am -DfailIfNoTests=false -Dtest=ReportMigrationTest#reportGridRendererBuildsLegacyMakeReportCellsFromFlatRows,ReportMigrationTest#reportGridRendererKeepsHeadersAndPagingForEmptyRows test`
  - Passed: 2 tests.
- Broader report: `docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-report -am -DfailIfNoTests=false -Dtest=ReportMigrationTest test`
  - Passed: 22 tests.
- Harness: `python scripts/check_repo_harness.py`
  - Passed.
- Patch hygiene: `git diff --check`
  - Passed.
- Runtime build: `docker compose up -d --build`
  - Passed; backend Docker build ran `mvn -DskipTests package` successfully and frontend image built.
- Runtime refresh: `docker compose up -d --force-recreate backend frontend`
  - Passed; backend and frontend containers were recreated.
- Runtime status: `docker compose ps`
  - `backend`, `frontend`, `mysql`, and `redis` were running; MySQL and Redis were healthy.
- Backend smoke: `curl -sf http://localhost:8080/test`
  - Passed; returned seeded order JSON.
- Frontend smoke: `curl -sfI http://localhost:8081/`
  - Passed; returned `HTTP/1.1 200 OK`.

## Downgrades / Risks

- Direct host Maven was not used because the host Java is currently 1.8 and this Java 17 project fails there with `invalid target release: 17`; validation used a JDK17 Maven container.
