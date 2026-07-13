# Legacy Chart Y Boundary

## Prompt

Continue aligning old-page layout, style, and interaction behavior, committing
every change atomically and retaining View-first rendering and code reuse.

## Scope

- Restore the configured 50% upper value-axis boundary gap.
- Share the calculation across top-level and compact charts.
- Preserve legend-filtered and empty-axis behavior.

## Changed Files

- `frontend/src/legacyChartGeometry.ts`
- `frontend/src/legacyChartGeometry.test.ts`
- `frontend/src/LegacyChartPanel.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T20-24-51Z-legacy-chart-y-boundary.md`

## Legacy Evidence

- Both old `swchartLine.js` option builders configure
  `yAxis.boundaryGap` as `[0, '50%']`.
- ECharts 3.1.7 `axisHelper.getScaleExtent` parses each percentage against the
  raw data span, applies the lower/upper additions, and only then applies the
  default cross-zero rule.
- Legend filtering precedes chart scale calculation; no visible data retains
  the engine's empty `0..1` value-axis fallback.

## Implementation

- Moved value-domain calculation into a 13-line pure geometry helper rather
  than adding more coordinate branches to the shared Vue component.
- Applied the exact boundary-gap/cross-zero order to finite visible values and
  retained the existing empty/all-zero fallback.
- Reused the result for compact and top-level rendering; View projection,
  series metadata, tooltip, legend, and bar layout remain unchanged.

## Validation

- Full frontend suite: 159 tests passed, including direct positive, negative,
  mixed-sign, empty, and non-finite geometry cases.
- `npm run build`: Vue type-check and Vite production build passed.
- Frontend Docker build and forced recreation passed. Deployed image id:
  `sha256:7c373fa950680e22da3567fa7eebf360fd16adefc59fb098e421fae62f438be2`.
- Authenticated `/view103` rendered a `93,750` top tick for Price, 100 bars,
  and the existing `62,500` tail value.
- Hiding Price recalculated Amount to a `0..0.375` range; hiding both series
  retained `0..1` and rendered neither pointer nor tooltip.
- Authenticated `/view100` also rendered `93,750` above its eight list-derived
  categories.
- At 390x844 the 328px compact pane, `93,750` tick, and document stayed within
  the 390px viewport.

## Runtime Evidence

- `artifacts/runs/20260714-legacy-chart-y-boundary/chart-y-boundary-desktop.png`
- `artifacts/runs/20260714-legacy-chart-y-boundary/chart-y-boundary-mobile.png`

## Risks And Follow-Ups

- ECharts 3.1.7's subsequent interval nicifying may round the expanded bound;
  that tick-generation behavior remains a separate parity slice.
- Realtime mixed-type `stack: 'a'` remains unimplemented and requires the
  engine's positive/negative and bar-layout rules rather than simple summing.
- Complete page parity remains subject to further View-by-View comparison.
- The existing authenticated browser session was reused, so no fresh CAPTCHA
  was generated or read for this frontend-only slice.
