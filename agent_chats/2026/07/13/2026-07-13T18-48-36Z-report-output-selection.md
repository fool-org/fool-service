# Report Output Selection

## Prompt

Continue aligning old-page layout, style, and interaction behavior, with every
change committed atomically. Keep report behavior driven by loaded View
metadata and avoid unnecessary abstractions.

## Scope

- Preserve the selected report output when later or duplicate outputs are
  added.
- Retain first-option selection for the first output and all existing output
  mutation helpers.

## Changed Files

- `frontend/src/ReportOutputSelector.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T18-48-36Z-report-output-selection.md`

## Legacy Evidence

- `mkreport.js add()` appends a new option to `#rtp-selected` but does not
  explicitly select each appended option.
- Duplicate column/type additions return before changing the selected option.
- The single-select list selects its first option once; later appended options
  leave the existing selection in place for move, delete, and sort commands.

## Implementation

- Detect whether the list already contains an output before appending.
- Select index zero only when the first output is created.
- Return immediately when `addReportOutput` reports a duplicate through its
  existing identity-preserving no-op contract.
- Removed the forced `findIndex` reselection. No helper, component, dependency,
  or DTO behavior was added.

## Validation

- Focused payload/report-output contracts: 85 tests passed.
- Full frontend suite: 156 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- Frontend-only Docker build and forced recreation passed. Deployed image id:
  `sha256:2a08743118b9adba9de20a633bc42247a95f9c934ff2ec1849fa6c087168ebb2`.
- `python scripts/check_repo_harness.py`, `git diff --check`, backend `/test`,
  frontend `/`, and Compose service-state checks passed.
- Temporarily changed runtime View 100 from `viewWithChart` to the default list
  template and restarted the backend solely for authenticated browser proof.
- Adding `Order ID[原值]` selected the first output. Adding `Symbol[原值]`
  retained `Order ID[原值]`; adding Symbol again retained two options and the
  same selection. The next Descending command changed only Order ID.
- Restored View 100 to `VIEW_FILE=990001`, restarted the backend, and verified
  the Chart tab, 11 table rows, and absence of the Statistics command.

## Runtime Evidence

- `artifacts/runs/20260714-report-output-selection/selection-preserved.png`

## Risks And Follow-Ups

- The seeded runtime normally uses `viewWithChart`; the temporary database
  change was fully restored and is not represented in source or seed files.
- The CAPTCHA-backed runtime doctor was not run because the existing
  authenticated browser session covered this frontend-only interaction and no
  fresh CAPTCHA was generated.
