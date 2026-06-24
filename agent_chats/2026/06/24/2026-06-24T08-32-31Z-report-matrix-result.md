# Report MatrixResult Unsupported Surface

## Prompt

- Continue the `/goal`: migrate against `../FoolFrame`, keep Docker/Vue runtime usable, and commit atomically.

## Scope

- Compared `../FoolFrame/src/Server/SWRPT01-Soway.Report/Views/MatrixResult.cs`.
- Migrated the public `MatrixResult` type and its legacy unsupported `Add()` method surface.
- Java exposes `MatrixResult#add()` and throws `UnsupportedOperationException`, matching the legacy `NotImplementedException` behavior.

## Changed Files

- `fool-report/src/main/java/org/fool/framework/report/MatrixResult.java`
- `fool-report/src/test/java/org/fool/framework/report/ReportMigrationTest.java`
- `docs/migration/foolframe-parity.md`

## TDD Evidence

- Red test:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -q -pl fool-report -am -Dtest=ReportMigrationTest#matrixResultKeepsLegacyUnsupportedAddSurface -DfailIfNoTests=false test`
  - Result: test compilation failed because `MatrixResult` did not exist.
- Green focused test:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -q -pl fool-report -am -Dtest=ReportMigrationTest#matrixResultKeepsLegacyUnsupportedAddSurface -DfailIfNoTests=false test`
  - Result: passed.
- Report regression:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -q -pl fool-report -am -Dtest=ReportMigrationTest -DfailIfNoTests=false test`
  - Result: `ReportMigrationTest`: 16 tests, 0 failures, 0 errors.

## Runtime / Skips

- No runtime or frontend behavior changed in this slice.
- Repository harness:
  - `python scripts/check_repo_harness.py`
  - Result: passed.
- Whitespace check:
  - `git diff --check`
  - Result: passed.
- Docker rebuild and Vue build were deferred to broader runtime validation.

## Follow-ups

- Continue `SWRPT01-Soway.Report` query/export integration around rendered report output.
- Continue the broader FoolFrame parity checklist in `docs/migration/foolframe-parity.md`.
