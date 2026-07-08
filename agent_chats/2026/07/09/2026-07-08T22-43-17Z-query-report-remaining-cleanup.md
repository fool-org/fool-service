# Query Report Remaining Cleanup

## Prompt

Continue the FoolFrame migration without adding speculative code.

## Scope

- Migration state docs only.
- Do not add query/report code for legacy surfaces that are already covered or
  are empty shells in `../FoolFrame`.

## Source Check

- `../FoolFrame/src/Server/SWDQ01-Soway.Query/QueryReport.cs`,
  `QueryParameter.cs`, and `QueryInsFac.cs` are already represented by
  `QueryReportDefinition`, `QueryParameter`, and `QueryInsFac` tests.
- `../FoolFrame/src/Server/SWRPT01-Soway.Report/IReportSource.cs` and
  `ReportFactory.cs` are empty shells.
- `ReportMigrationTest` already covers source-row matrix construction and
  legacy unsupported getter/no-op setter report surfaces.

## Changes

- Replaced stale broad `SWDQ01-Soway.Query` and `SWRPT01-Soway.Report`
  remaining-work bullets with "continue only when a new legacy surface is
  identified" wording.
- Added the cleanup to recent parity increments and task state.

## Validation

- PASS: `python scripts/check_repo_harness.py`.
- PASS: `git diff --check`.

## Risks

- This does not claim all migration work is done. It only removes stale
  remaining-work text for areas that the current module map and tests already
  cover.
