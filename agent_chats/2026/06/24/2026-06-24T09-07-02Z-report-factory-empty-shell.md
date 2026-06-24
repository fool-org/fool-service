# ReportFactory empty shell parity

## Prompt

- Continue the active FoolFrame migration goal with Docker/Vue runtime kept running and scoped atomic commits.

## Scope

- Compared `../FoolFrame/src/Server/SWRPT01-Soway.Report/ReportFactory.cs`.
- Preserved the legacy empty-shell class shape in `fool-report`.

## Changes

- Added `org.fool.framework.report.ReportFactory` as an empty class with no fields or methods.
- Added `ReportMigrationTest.reportFactoryPreservesLegacyEmptyShell`.
- Updated `docs/migration/foolframe-parity.md` to record `ReportFactory`/`IReportSource` empty shell parity and narrow the remaining report work to source adapters plus query/export wiring.

## Validation

- Red test:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -q -pl fool-report -am -Dtest=ReportMigrationTest#reportFactoryPreservesLegacyEmptyShell -DfailIfNoTests=false test`
  - Result: failed because `org.fool.framework.report.ReportFactory` did not exist.
- Green test:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -q -pl fool-report -am -Dtest=ReportMigrationTest#reportFactoryPreservesLegacyEmptyShell -DfailIfNoTests=false test`
  - Result: passed.
- Focused report migration suite:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -q -pl fool-report -am -Dtest=ReportMigrationTest -DfailIfNoTests=false test`
  - Result: passed.

## Runtime Evidence

- Runtime was not changed in this slice.
- Before the slice, `docker compose ps` showed backend/frontend/mysql/redis running, with MySQL and Redis healthy.

## Risks

- Full reactor `mvn test`, frontend `npm test`, frontend `npm run build`, and a fresh Compose rebuild were not rerun for this empty-shell class slice.

## Follow-ups

- Continue remaining report work around table source adapters and query/export integration around the rendered report grid.
