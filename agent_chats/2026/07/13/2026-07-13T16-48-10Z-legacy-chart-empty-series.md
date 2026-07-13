# Legacy Empty Chart Series Parity

## Prompt

Continue aligning old-page layout, style, and interaction behavior, with every
change committed atomically. Inspect the old chart View and controller before
changing the Vue zero-series rendering.

## Scope

- Retain the old chart surface after a query returns no rows or chart series.
- Remove Vue-only empty copy and numeric horizontal-label fallbacks.

## Changed Files

- `frontend/src/LegacyChartPanel.vue`
- `frontend/src/ViewListPanel.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T16-48-10Z-legacy-chart-empty-series.md`

## Legacy Evidence

- `../FoolFrame/src/Web/views/viewWithChart.jade` always provides the Chart tab
  pane and defines no zero-series message.
- `../FoolFrame/src/Web/public/javascripts/app/viewWithChart.js` initializes
  ECharts from every list-query callback, including an empty item array.
- `swchartLine.js` returns empty `xAxis.data` and `series` arrays for that case,
  leaving the chart surface and axes without invented horizontal labels.

## Implementation

- Mounted `LegacyChartPanel` whenever the chart tab is active, independent of
  series count, and removed `暂无图表数据。`.
- Restricted SVG horizontal-axis text to actual legacy axis labels and removed
  the numeric index fallback.
- Reused the existing chart adapter and renderer; added no DTO, API, state,
  component, dependency, or CSS changes.

## Validation

- Focused source/workflow suites: 130 tests passed.
- Full frontend suite: 155 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- Docker frontend production build and forced no-dependency recreation passed.
  Deployed image id:
  `sha256:d48a0e5496fb55635c4241e1f1a1d6c5c303fea50f04de21eeed4e1a730254d6`.
- Authenticated `/view100` queried `NO_CHART_MATCH_20260714`, returned
  `共0条记录`, and switched to the Chart tab. Runtime retained one accessible
  SVG chart with visible axes, no `暂无图表数据。` copy, and no generated
  horizontal index label.
- Stable 1440x1000 and 390x844 screenshots show the empty chart surface without
  clipping or overlap. Repository harness validation passed; frontend `/` and
  backend `/test` returned HTTP 200. MySQL and Redis remained healthy and
  `db-migrate` remained `Exited (0)`.

## Runtime Evidence

- `artifacts/runs/20260714-legacy-chart-empty-series/chart-empty-desktop.png`
- `artifacts/runs/20260714-legacy-chart-empty-series/chart-empty-mobile.png`

## Risks And Follow-Ups

- The shared SVG uses a deterministic 0-to-1 vertical domain when no values
  exist, matching the visible empty-axis role of the old ECharts surface.
- The CAPTCHA-backed runtime doctor was not rerun because it would generate a
  new CAPTCHA; the existing authenticated browser session covered this slice.
