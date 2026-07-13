# Legacy Chart Bar Width

## Prompt

Continue aligning old-page layout, style, and interaction behavior, committing
every change atomically and retaining View-first rendering and code reuse.

## Scope

- Compare old `swchartLine.js` bar-width behavior with the deployed shared SVG.
- Restore the top-level chart's 15px cap without changing the compact realtime
  chart branch or duplicating the renderer.

## Changed Files

- `frontend/src/LegacyChartPanel.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T19-27-15Z-legacy-chart-bar-width.md`

## Legacy Evidence

- `swchartLine.js getOption` sets `barMaxWidth: 15` on `EditType=13` series used
  by top-level `viewWithChart`.
- The Sudoku realtime `LineChartController` creates its bar series without a
  `barMaxWidth`, so the two old entrypoints intentionally differ.
- The deployed shared SVG used `width="28"` viewBox units. Browser measurement
  showed those units rendering as `51.877px` at 1440x1000 and `11.822px` at
  390x844, so replacing 28 with 15 units would not reproduce the pixel cap.

## Implementation

- Reused the component's existing `ResizeObserver` for all chart instances and
  retained its compact-only viewBox adjustment.
- Recorded the rendered SVG width and converted the top-level 15px maximum into
  current viewBox units. Existing category bandwidth remains the secondary cap.
- Kept the compact branch's previous 28-unit cap and added a stable `chart-bar`
  class for runtime verification. No API, View projection, dependency,
  component, or business DTO path was added.

## Validation

- Focused chart/source contracts: 82 tests passed.
- Full frontend suite: 156 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- `python scripts/check_repo_harness.py` and `git diff --check` passed.
- Frontend Docker build and forced recreation passed. Deployed image id:
  `sha256:325b1a8342db0db26fff14b5db7aebf3f2e8f47b0b6ad3934ab3ec7cc177d20c`.
- Backend `/test`, frontend `/view100`, and Compose service checks passed;
  MySQL/Redis were healthy and `db-migrate` remained `Exited (0)`.
- Authenticated `/view100` rendered eight bars at exactly `15px` at both
  1440x1000 and 390x844. The responsive viewBox widths were `8.096` and
  `35.5263`, proving the cap is converted rather than visually approximated.
- Horizontal endpoints remained `52/702`, the axis name stayed separate from
  the final category, and mobile first/last bars remained inside the SVG.
- Authenticated `/view103` retained one compact chart with five bars at the
  previous `width="28"` internal cap; browser error logs were empty.

## Runtime Evidence

- `artifacts/runs/20260714-legacy-chart-bar-width/order-chart-desktop.png`
- `artifacts/runs/20260714-legacy-chart-bar-width/order-chart-mobile.png`

## Risks And Follow-Ups

- First/last bar clipping at the plot boundary remains a separate comparison
  against ECharts grid clipping and was not mixed into this width-only change.
- The existing authenticated browser session was reused, so no fresh CAPTCHA
  was generated or read for this frontend-only slice.
