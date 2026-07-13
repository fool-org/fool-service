# Legacy Sudoku Empty Chart Series Parity

## Prompt

Continue aligning old-page layout, style, and interaction behavior, with every
change committed atomically. Apply the verified zero-series chart behavior to
the old Sudoku linechart partial without duplicating the chart renderer.

## Scope

- Keep Sudoku linechart panels mounted when their query has no chart series.
- Preserve existing panel loading, sizing, refresh, and shared chart behavior.

## Changed Files

- `frontend/src/SudokuPanels.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T16-51-40Z-legacy-sudoku-chart-empty-series.md`

## Legacy Evidence

- `../FoolFrame/src/Web/views/includes/linechart.jade` always renders the
  `.sw-partialchart` container.
- `../FoolFrame/src/Web/public/javascripts/app/swchartLine.js` initializes
  ECharts before its first data update and defines no empty-state replacement.

## Implementation

- Removed only the `series.length` condition from the existing Sudoku
  `LegacyChartPanel` branch.
- Reused the shared zero-series SVG behavior from the preceding chart slice;
  no duplicated view, chart, or data state was added.
- Added no DTO, API, component, dependency, or CSS changes.

## Validation

- Full frontend suite: 155 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- Docker frontend production build and forced no-dependency recreation passed.
  Deployed image id:
  `sha256:2056a241b0869f5bb6f78ed2c9df3f58b88a86a90df4411f5ebc79b8f0d4213f`.
- Authenticated `/view103` rendered one accessible `Price Chart` SVG from the
  View-defined `./includes/linechart` panel, retained its legend, update time,
  and Refresh command, and continued rendering the surrounding four panels.
- Stable 1440x1000 and focused 390x844 screenshots show the compact chart and
  following map without clipping or overlap. Repository harness validation
  passed; frontend `/` and backend `/test` returned HTTP 200. MySQL and Redis
  remained healthy and `db-migrate` remained `Exited (0)`.

## Runtime Evidence

- `artifacts/runs/20260714-legacy-sudoku-chart-empty-series/sudoku-chart-desktop.png`
- `artifacts/runs/20260714-legacy-sudoku-chart-empty-series/sudoku-chart-mobile.png`

## Risks And Follow-Ups

- The seeded Sudoku linechart currently has series data; the empty-series branch
  is covered by the source contract and the already runtime-proven shared SVG.
- The CAPTCHA-backed runtime doctor was not rerun because it would generate a
  new CAPTCHA; the existing authenticated browser session covered this slice.
