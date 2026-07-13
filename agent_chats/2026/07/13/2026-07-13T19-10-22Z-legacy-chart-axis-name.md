# Legacy Chart Axis Name

## Prompt

Continue aligning old-page layout, style, and interaction behavior, committing
every change atomically and retaining View-first rendering and code reuse.

## Scope

- Compare old `swchartLine.js` horizontal-axis metadata with the deployed
  shared chart.
- Restore the missing View-derived axis name for top-level and Sudoku charts.

## Changed Files

- `frontend/src/viewWorkflow.ts`
- `frontend/src/viewWorkflow.test.ts`
- `frontend/src/LegacyChartPanel.vue`
- `frontend/src/style.css`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T19-10-22Z-legacy-chart-axis-name.md`

## Legacy Evidence

- `swchartLine.js getchartoption` reads the first result row and assigns the
  `EditType=11` item's `PrpShowName` to `option.xAxis.name`.
- Every `EditType=11` item's `FmtValue` remains a category value in
  `option.xAxis.data`.
- Deployed `/view100` rendered the category values but omitted the View field
  name `Order ID` from the horizontal axis.

## Implementation

- Added `axisName` to the existing shared `LegacyChartData` projection.
- Populated it only from the first row's chart-axis item, preserving the old
  initialization boundary and the existing category-label loop.
- Rendered the metadata text at the shared SVG horizontal-axis end. No business
  property name, extra request, component, or DTO branch was added.

## Validation

- Focused View/chart contracts: 127 tests passed.
- Full frontend suite: 156 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- `python scripts/check_repo_harness.py` and `git diff --check` passed.
- Frontend Docker build and forced recreation passed. Deployed image id:
  `sha256:5017fcc39976dbb0eae24d9d1d5d830d98b52f97828631174354e123521ce678`.
- Backend `/test`, frontend `/`, and Compose service-state checks passed;
  MySQL/Redis were healthy and `db-migrate` remained `Exited (0)`.
- Authenticated `/view100` rendered exactly one `.chart-axis-name` with
  `Order ID` at both 1440x1000 and 390x844. Screenshots showed it separated
  from the final `1001` category label without clipping or layout shift.

## Runtime Evidence

- `artifacts/runs/20260714-legacy-chart-axis-name/order-chart-desktop.png`
- `artifacts/runs/20260714-legacy-chart-axis-name/order-chart-mobile.png`

## Risks And Follow-Ups

- The shared SVG approximates ECharts' axis-name placement while preserving
  the same metadata and visible end-of-axis role.
- The existing authenticated browser session was reused, so no fresh CAPTCHA
  was generated or read for this frontend-only slice.
