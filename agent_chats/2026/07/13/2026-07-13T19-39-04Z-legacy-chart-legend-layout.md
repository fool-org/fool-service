# Legacy Chart Legend Layout

## Prompt

Continue aligning old-page layout, style, and interaction behavior, committing
every change atomically and retaining View-first rendering and code reuse.

## Scope

- Verify whether old ECharts clips first/last bars at the plot boundary.
- Restore the shared chart's old 20% right grid and right-middle legend layout.

## Changed Files

- `frontend/src/LegacyChartPanel.vue`
- `frontend/src/style.css`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T19-39-04Z-legacy-chart-legend-layout.md`

## Legacy Evidence

- The old repository declares `echarts ^3.1.7`. Its published `BarView` adds
  bar rectangles directly to the chart group without a grid clip path, so the
  shared SVG's visible first/last bar halves were already compatible and were
  not changed.
- Both `swchartLine.js getOption` and the realtime `LineChartController` set
  `grid.right` to `20%`, with the legend at `left: right`, `top: middle`.
- ECharts 3.1.7 `LegendModel` defaults to `orient: horizontal`. The deployed Vue
  CSS instead kept the legend in normal flow below the SVG.

## Implementation

- Replaced the fixed 18-unit right plot margin with one shared computed 20%
  margin used by axes, category coordinates, labels, and bar bandwidth.
- Positioned the existing metadata legend absolutely at the pane's right-middle
  and retained its horizontal desktop orientation.
- On viewports up to 600px, stacked the same entries vertically and reduced the
  right inset to 4px. Runtime measurement puts the legend fully inside the
  reserved column without changing series state or labels.
- Added no request, View projection, chart dependency, component, or business
  DTO branch.

## Validation

- Focused chart/source contracts: 82 tests passed.
- Full frontend suite: 156 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- `python scripts/check_repo_harness.py` and `git diff --check` passed.
- Frontend Docker build and forced recreation passed. Deployed image id:
  `sha256:54abdc71589b6d2fc0abab9dce7228f7072cad571c3c1959c783d9ca9b1756f4`.
- Backend `/test`, frontend `/view100`, and Compose service checks passed;
  MySQL/Redis were healthy and `db-migrate` remained `Exited (0)`.
- Authenticated `/view100` at 1440x1000 rendered a horizontal right-middle
  legend with center delta `0`, a `20%` plot-right fraction, and 136.097px
  separation from the plot. Bars remained 15px and no value label overlapped.
- At 390x844, the legend stayed right-middle, switched to a column, started
  0.441px after the plot endpoint, retained a 5px pane inset, and overlapped no
  value labels. Bars remained 15px.
- Authenticated `/view103` retained a 200px compact pane, five bars, a dynamic
  `1225x160` viewBox, a `20%` right margin, a horizontal centered legend, and no
  browser errors.

## Runtime Evidence

- `artifacts/runs/20260714-legacy-chart-legend-layout/order-chart-desktop.png`
- `artifacts/runs/20260714-legacy-chart-legend-layout/order-chart-mobile.png`

## Risks And Follow-Ups

- ECharts legends are selectable by default; the shared Vue legend remains
  display-only. Series toggling is the next separate interaction-parity item.
- The existing authenticated browser session was reused, so no fresh CAPTCHA
  was generated or read for this frontend-only slice.
