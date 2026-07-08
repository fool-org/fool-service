# Save Report Parity Drift

## Prompt

Continue the FoolFrame migration while keeping the View/data workflow aligned
with the legacy implementation and avoiding invented behavior.

## Scope

- Checked FoolFrame `saverpt` server behavior before implementing report
  persistence.
- Confirmed `Soway.Server/Report/HandlerSaveReport.cs` has an empty
  `ImplementBusinessLogic()` and `DataService.SaveReport()` only invokes that
  handler and returns the base `Result`.
- Removed saved report metadata persistence and saved-report execution from the
  remaining server parity list.

## Changed Files

- `docs/migration/foolframe-parity.md`
- `tasks.md`

## Validation

- Source check: `../FoolFrame/src/Server/Soway.Server/Report/HandlerSaveReport.cs`.
- Source check: `../FoolFrame/src/Server/Soway.Server/DataService.cs`.
- Source check: `../FoolFrame/src/Server/Soway.Server/IDataService.cs`.
- Green: `git diff --check`.
- Green: `python scripts/check_repo_harness.py`.

## Skipped Checks

- Maven and Docker runtime checks were not rerun because this slice only
  corrects migration-state documentation against legacy source truth.

## Risks

- Export wiring and table-source behavior remain open report work.
