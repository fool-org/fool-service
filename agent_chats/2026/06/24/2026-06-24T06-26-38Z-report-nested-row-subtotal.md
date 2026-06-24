# Report Nested Subtotal Coverage

## Prompt

- Continue the active goal: bring the environment up with Docker, complete migration against `../FoolFrame`, use Vue for the frontend, and keep atomic commits timely.

## Scope

- Compared `../FoolFrame/src/Server/SWRPT01-Soway.Report/Views/MatrixTableFactory.cs` with the Java `MatrixTableFactory`.
- Added focused coverage for nested row and column header static subtotal behavior.
- Verified the existing Java implementation already scopes nested static subtotal cells to sibling leaf rows/columns.

## Changes

- Added `ReportMigrationTest#createMatrixTableScopesNestedRowStaticSubtotalsToSiblingLeaves`.
- Added `ReportMigrationTest#createMatrixTableScopesNestedColumnStaticSubtotalsToSiblingLeaves`.
- The row test covers a two-level row header (`region` / `city`), a static `Subtotal` on the city level, one column header (`year`), and one value cell (`amount`).
- The column test covers a two-level column header (`year` / `quarter`), a static `Subtotal` on the quarter level, one row header (`region`), and one value cell (`amount`).
- Left the mixed `docs/migration/foolframe-parity.md` status update unstaged for a separate status commit.

## TDD

- Focused test command:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-report -am -Dtest=ReportMigrationTest#createMatrixTableScopesNestedRowStaticSubtotalsToSiblingLeaves -DfailIfNoTests=false test`
  - Result: `ReportMigrationTest`: 1 test, 0 failures, 0 errors.
- Focused test command:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-report -am -Dtest=ReportMigrationTest#createMatrixTableScopesNestedColumnStaticSubtotalsToSiblingLeaves -DfailIfNoTests=false test`
  - Result: `ReportMigrationTest`: 1 test, 0 failures, 0 errors.
- Both new coverage cases passed immediately, so no production code change was needed for this edge.

## Verification

- Focused Report migration regression:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-report -am -Dtest=ReportMigrationTest -DfailIfNoTests=false test`
  - Result: `ReportMigrationTest`: 12 tests, 0 failures, 0 errors.
- Repo hygiene checks:
  - `python scripts/check_repo_harness.py`
  - Result: passed.
  - `git diff --check`
  - Result: passed.
  - `git diff --cached --check`
  - Result: passed.

## Runtime Evidence

- Not rerun for this slice. This is `fool-report` unit-level coverage and does not change the running Compose API or Vue frontend.

## Risks

- This covers nested row/column static subtotal sibling scope only; additional matrix subtotal combinations, table source adapters, and query/export integration remain open.
- Atomic commit is still blocked while this workspace cannot write `.git/index.lock`.

## Follow-ups

- Continue `SWRPT01-Soway.Report` table source adapter and query/export parity.
- Commit this Report coverage atomically once `.git` writes are available.
