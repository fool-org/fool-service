# Legacy Chart Tooltip Hide Delay

## Prompt

Continue aligning old-page layout, style, and interaction behavior, committing
every change atomically while retaining View-first rendering and code reuse.

## Scope

- Compare the old ECharts tooltip hide timing with the shared Vue chart.
- Restore the same cancellable delay for top-level and compact plot exit.
- Keep explicit legend/no-data cleanup immediate and avoid adding a component,
  composable, request, DTO branch, rendering path, or dependency.

## Changed Files

- `frontend/src/LegacyChartPanel.vue`
- `frontend/src/style.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/14/2026-07-14T00-01-57Z-legacy-chart-tooltip-hide-delay.md`

## Legacy Evidence

- ECharts 3.1.7 `TooltipModel.defaultOption` sets `showDelay: 0` and
  `hideDelay: 100`.
- `TooltipContent.hideLater` starts one timeout and marks the tooltip no longer
  showing so repeated outside events do not extend it.
- `TooltipContent.show` clears that timeout when pointer movement returns to a
  valid target.
- In the deployed Vue baseline, `/view100` showed one tooltip inside the plot,
  then moving into the SVG title whitespace yielded tooltip counts
  `immediate=0, 10ms=0, 50ms=0`.

## Implementation

- Added one component-local timer for out-of-plot movement and SVG mouseleave.
- Valid plot movement cancels the pending timer; repeated outside movement
  reuses it rather than extending the 100ms delay.
- Existing `hideAxisTooltip` remains the immediate path for legend selection,
  no visible series, and final timer cleanup. Unmount clears the timer.
- The shared component grew from 354 to 370 lines; no one-use abstraction or
  duplicate tooltip path was introduced.

## Validation

- Focused `npm test -- src/style.test.ts`: 10 tests passed.
- Full `npm test`: 14 files and 176 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- `docker compose build frontend` passed; the service was attached to the new
  image with `docker compose up -d --no-deps --force-recreate frontend`:
  `sha256:57e7266ab950e6205ed61401f78def30e5e44e3ba77485c986628f7ea53ec5cb`.
- Backend `/test` passed; MySQL/Redis were healthy and `db-migrate` remained
  `Exited (0)`.
- Authenticated desktop `/view100` measured tooltip counts
  `inside=1, immediate=1, 50ms=1, 120ms=0` after moving from the plot to title
  whitespace.
- Returning to the plot at 50ms retained one tooltip and changed its visible
  category to `9751160`; a later complete 120ms leave reduced the count to zero.
- At 390x844, `/view103` passed the same compact-chart `1/1/1/0` timing
  sequence. Browser logs were empty.

## Runtime Evidence

- `artifacts/runs/20260714-legacy-chart-tooltip-hide-delay/order-tooltip-inside-desktop.png`
- `artifacts/runs/20260714-legacy-chart-tooltip-hide-delay/order-tooltip-hidden-desktop.png`
- `artifacts/runs/20260714-legacy-chart-tooltip-hide-delay/compact-tooltip-inside-mobile.png`
- `artifacts/runs/20260714-legacy-chart-tooltip-hide-delay/compact-tooltip-hidden-mobile.png`

## Skipped Checks And Risks

- The full old FoolFrame application was not booted; its pinned ECharts 3.1.7
  source supplied the legacy timer contract.
- The existing authenticated session was reused, so no fresh CAPTCHA was
  generated or read for this frontend-only slice.
