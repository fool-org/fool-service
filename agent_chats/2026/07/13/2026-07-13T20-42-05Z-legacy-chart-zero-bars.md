# Legacy Chart Zero Bars

## Prompt

Continue aligning old-page layout, style, and interaction behavior, committing
every change atomically and retaining View-first rendering and code reuse.

## Scope

- Restore ECharts 3.1.7's default zero-value bar height.
- Reuse the shared renderer for top-level and compact charts.
- Preserve natural nonzero geometry, stacking, and responsive layout.

## Changed Files

- `frontend/src/LegacyChartPanel.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T20-42-05Z-legacy-chart-zero-bars.md`

## Legacy Evidence

- ECharts 3.1.7 `BarSeries.defaultOption.barMinHeight` is `0`.
- Its Cartesian bar layout only raises a bar when its absolute height is below
  `barMinHeight`; a zero value therefore remains zero-height by default.
- Neither old `swchartLine.js` branch overrides `barMinHeight`.

## Implementation

- Removed the Vue-only `Math.max(1, ...)` floor from the shared SVG bar-height
  calculation. Zero values now produce exact zero-height rectangles, while
  nonzero values remain proportional to the existing View-derived scale.
- Added a source-level regression assertion without introducing a chart
  dependency, business DTO branch, or duplicate renderer.

## Validation

- Full frontend suite: 163 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- Frontend Docker build and forced recreation passed. Deployed image id:
  `sha256:85a59c252247c03e2f0ec9b5d590eef35f726f094fea6f1ed73e0f3357538873`.
- Authenticated `/view103` rendered 100 Amount bars: 99 at exact height `0`
  and one at natural height `0.00019000000000346517`. It retained the stacked
  Price endpoint y=`66.49981` and 200 raw labels.
- Authenticated `/view100` rendered eight Amount bars: six at height `0` and
  two natural nonzero heights `0.003539999999986776` / `0.0005900000000167438`.
- At 390x844 `/view103` retained the same 99/1 split; the chart stayed between
  x=`44..346` and document scroll width remained exactly 390px.

## Runtime Evidence

- `artifacts/runs/20260714-legacy-chart-zero-bars/chart-zero-bars-desktop.png`
- `artifacts/runs/20260714-legacy-chart-zero-bars/chart-zero-bars-mobile.png`

## Risks And Follow-Ups

- Old top-level series use metadata names as stack ids; duplicate-name metadata
  remains unseeded and requires its own comparison before claiming that edge.
- Complete page parity remains subject to further View-by-View comparison.
- The existing authenticated browser session was reused, so no fresh CAPTCHA
  was generated or read for this frontend-only slice.
