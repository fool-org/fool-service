# Legacy Chart Nice Ticks

## Prompt

Continue aligning old-page layout, style, and interaction behavior, committing
every change atomically and retaining View-first rendering and code reuse.

## Scope

- Restore ECharts 3.1.7 interval nicifying after the value-axis boundary gap.
- Render the resulting variable tick count in both chart branches.

## Changed Files

- `frontend/src/legacyChartGeometry.ts`
- `frontend/src/legacyChartGeometry.test.ts`
- `frontend/src/LegacyChartPanel.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T20-29-19Z-legacy-chart-nice-ticks.md`

## Legacy Evidence

- ECharts 3.1.7 `axisHelper.niceScaleExtent` passes the boundary-expanded
  extent to `IntervalScale.niceExtent` with the default split number of five.
- `niceTicks` computes `span / splitNumber`, selects a decimal interval using
  the old 1/2/3/5/10 thresholds, and `niceExtent` rounds unfixed limits outward.
- `getTicks` emits every interval plus any outer extent endpoint, so the final
  number of labels is not fixed at five.

## Implementation

- Extended the existing pure geometry helper with the old interval thresholds,
  ten-decimal rounding, outward extent calculation, and descending tick list.
- Replaced the shared component's hard-coded five-way interpolation with the
  tested scale ticks; y coordinates now use the same nicified extent.
- Added no rendering branch, dependency, or data-field fallback.

## Validation

- Full frontend suite: 160 tests passed. Geometry tests cover Price, Amount,
  and empty-axis scale/tick results.
- `npm run build`: Vue type-check and Vite production build passed.
- Frontend Docker build and forced recreation passed. Deployed image id:
  `sha256:3fc65f38e561f6a0076737fc68ecfe544524458009f104be5f2aa50fc30a327c`.
- Authenticated `/view103` rendered six `100,000` through `0` ticks at `20,000`
  intervals while retaining 100 rolling bars.
- Hiding Price rendered five `0.4` through `0` ticks at `0.1`; hiding both
  rendered six `1` through `0` ticks at `0.2` and no axis pointer.
- Authenticated `/view100` rendered the same six Price ticks above its eight
  list-derived categories.
- At 390x844 all six tick labels stayed inside the 328px compact pane and the
  document scroll width remained exactly 390px.

## Runtime Evidence

- `artifacts/runs/20260714-legacy-chart-nice-ticks/chart-nice-ticks-desktop.png`
- `artifacts/runs/20260714-legacy-chart-nice-ticks/chart-nice-ticks-mobile.png`

## Risks And Follow-Ups

- Realtime mixed-type `stack: 'a'` remains unimplemented and requires the
  engine's positive/negative and bar-layout rules rather than simple summing.
- Complete page parity remains subject to further View-by-View comparison.
- The existing authenticated browser session was reused, so no fresh CAPTCHA
  was generated or read for this frontend-only slice.
