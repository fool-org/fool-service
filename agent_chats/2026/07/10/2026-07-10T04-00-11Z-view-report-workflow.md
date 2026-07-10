# View Report Workflow

## Prompt
- Continue the Docker/FoolFrame/Vue migration, keeping View rendering ahead of
  data binding, maximizing reuse, and controlling file size.

## Scope
- Compared the old list-page `view.jade`, `querylistdata.js`, and
  `mkreport.js` report path with the current Vue API Tools implementation.
- Moved the report command into the loaded View workflow and removed the three
  duplicated developer report panels from API Tools.
- Added `ViewReportPanel.vue`, which owns report state while reusing the shared
  action runner, payload builders, report metadata helpers, and response-cell
  renderer.
- Replaced raw report-column JSON and SQL filter inputs with metadata-driven
  output selection, output type, sorting, output order, and structured AND/OR
  condition controls.
- Kept the legacy `mkrpt` and `saverpt` routes; `saverpt` retains the old
  server's no-op success semantics.
- Reduced `App.vue` from 1,992 to 1,796 lines.

## Changed Files
- `frontend/src/App.vue`
- `frontend/src/ViewReportPanel.vue`
- `frontend/src/api.ts`
- `frontend/src/payload.ts`
- `frontend/src/payload.test.ts`
- `frontend/src/style.css`
- `frontend/src/useViewDataWorkflow.ts`
- `frontend/src/useViewDataWorkflow.test.ts`
- `frontend/src/viewWorkflow.ts`
- `frontend/src/viewWorkflow.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/10/2026-07-10T04-00-11Z-view-report-workflow.md`

## Validation
- `cd frontend && npm test` passed: 7 files, 130 tests.
- `cd frontend && npm run build` passed.
- `python scripts/check_repo_harness.py` passed.
- `docker compose build frontend` passed.
- `docker compose up -d --force-recreate --no-deps frontend` replaced the
  running frontend container.
- `python scripts/runtime_doctor.py` passed.
- `git diff --check` passed.

## Runtime Evidence
- Docker browser validation opened `Report` from `/view100`; the component
  loaded six candidate columns from View 100 before running report data.
- An unfiltered `mkrpt` returned eight rows. A structured `Symbol` contains
  `BTC` condition returned only order `1001` / `BTC-USDT`.
- Moving `Order ID` below `Symbol` changed the rendered report header order to
  `Symbol[原值]`, then `Order ID[原值]`.
- At 390px, document and viewport widths both remained 390px; the 860px report
  table stayed inside its 326px scroll container and the report panel did not
  overflow.
- Browser console warnings/errors: none.
- Screenshots:
  `artifacts/runs/20260710-view-report-workflow/view-report-desktop.png` and
  `artifacts/runs/20260710-view-report-workflow/view-report-mobile.png`.

## Risks
- The Vue report condition editor supports flat AND/OR sequences. The old
  experimental nested merge/split condition UI is not exposed.
- Report export buttons from the old Jade result modal remain outside this
  slice; the old page declared the buttons without a completed export path.

## Follow-ups
- Replace the main View toolbar's technical `QueryFilter` input with the
  existing metadata-safe keyword query path.
