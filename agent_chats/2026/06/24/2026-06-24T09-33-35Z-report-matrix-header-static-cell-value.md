# Report MatrixHeader and StaticCellValue migration

## Prompt

- Continue the active Docker/Vue/FoolFrame migration goal after the Event object-id column slice.

## Scope

- Compared legacy `../FoolFrame/src/Server/SWRPT01-Soway.Report/Views/MatrixHeader.cs`.
- Compared legacy `../FoolFrame/src/Server/SWRPT01-Soway.Report/Views/StaticCellValue.cs`.
- Added Java report view helper types for legacy `MatrixHeader` comparison/equality behavior and `StaticCellValue` storage shape.
- Updated FoolFrame parity notes and current report Java file counts.

## Changed Files

- `fool-report/src/main/java/org/fool/framework/report/MatrixHeader.java`
- `fool-report/src/main/java/org/fool/framework/report/StaticCellValue.java`
- `fool-report/src/test/java/org/fool/framework/report/ReportMigrationTest.java`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/06/24/2026-06-24T09-33-35Z-report-matrix-header-static-cell-value.md`

## Validation

- Red: `docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-report -am -DfailIfNoTests=false -Dtest=ReportMigrationTest#matrixHeaderKeepsLegacyComparableIdentityAndValueSemantics+staticCellValueKeepsLegacyInternalValueShape test`
  - Failed as expected with missing `MatrixHeader` and `StaticCellValue` classes.
- Green: `docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-report -am -DfailIfNoTests=false -Dtest=ReportMigrationTest#matrixHeaderKeepsLegacyComparableIdentityAndValueSemantics+staticCellValueKeepsLegacyInternalValueShape test`
  - Passed: 2 tests.
- Broader Report: `docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-report -am -DfailIfNoTests=false -Dtest=ReportMigrationTest test`
  - Passed: 22 tests.
- Harness: `python scripts/check_repo_harness.py`
  - Passed.
- Patch hygiene: `git diff --check`
  - Passed.
- Runtime build: `docker compose up -d --build`
  - Passed; backend Docker build ran `mvn -DskipTests package` successfully and compiled 31 `fool-report` source files.
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
- This does not implement broader report source adapters, query execution, or export integration.
