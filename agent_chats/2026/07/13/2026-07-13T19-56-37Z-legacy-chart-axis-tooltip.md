# Legacy Chart Axis Tooltip

## Prompt

Continue aligning old-page layout, style, and interaction behavior, committing
every change atomically and retaining View-first rendering and code reuse.

## Scope

- Replace the temporary per-point native titles with the old axis tooltip.
- Verify tooltip content, pointer, legend filtering, and responsive bounds.

## Changed Files

- `frontend/src/LegacyChartPanel.vue`
- `frontend/src/style.css`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T19-56-37Z-legacy-chart-axis-tooltip.md`

## Legacy Evidence

- Both `swchartLine.js getchartoption` and its realtime
  `LineChartController` set `tooltip.trigger` to `axis`.
- ECharts 3.1.7 defaults axis tooltips to mouse movement, a `#555` one-pixel
  line pointer, `rgba(50,50,50,0.7)` background, white 14px text, 5px padding,
  and 4px corners.
- Default content starts with the category value and then lists each active
  series name/value with its color marker. Legend filtering removes unselected
  series before tooltip content is built.

## Implementation

- Added one transparent plot hit rectangle to the shared SVG. Mouse movement
  selects the nearest View-derived category and positions one bounded HTML
  tooltip beside the pointer.
- Rendered the category label and only selected metadata series, using the
  existing series names, values, formatter, and color order.
- Added the old default pointer and tooltip treatment. All-hidden charts clear
  the overlay, and legend changes dismiss stale tooltip state.
- Removed native SVG titles and transparent line hit circles so the axis
  overlay is the single tooltip interaction for line, bar, and scatter data.
- Added no request, View projection, chart dependency, component, or business
  DTO branch; top-level and compact charts retain the shared renderer.

## Validation

- Full frontend suite: 156 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- Frontend Docker build and forced recreation passed. Deployed image id:
  `sha256:64429649dfedf4847c8acb9e3e5e6ce6b8a2ec65fbe17eeeb0512d57cdda6db8`.
- Backend `/test`, frontend `/view100`, and Compose service checks passed;
  MySQL/Redis remained healthy and `db-migrate` remained `Exited (0)`.
- Authenticated desktop `/view100` showed category `9851341`, Amount and Price
  rows, one `#555` pointer, the default dark/white tooltip colors, and no native
  titles or transparent line-hit circles; the overlay remained inside its pane.
- Hiding Price dismissed the current tooltip; the next category trigger showed
  one Amount row and the recalculated `1.5` y-axis maximum. With both series
  hidden, neither tooltip nor pointer rendered and the domain stayed `0..1`.
- At 390x844, the two-series tooltip remained inside the 330px chart pane.
- Authenticated `/view103` retained its 200px compact chart and showed two
  series rows plus one pointer inside the same pane.

## Runtime Evidence

- `artifacts/runs/20260714-legacy-chart-axis-tooltip/order-chart-axis-tooltip-desktop.png`
- `artifacts/runs/20260714-legacy-chart-axis-tooltip/order-chart-axis-tooltip-mobile.png`

## Risks And Follow-Ups

- Browser automation triggers movement through the chart hit target's locator;
  real pointer handling remains the same native `mousemove` listener.
- Complete chart and page parity remains subject to further comparison with the
  old View-driven workflows.
- The existing authenticated browser session was reused, so no fresh CAPTCHA
  was generated or read for this frontend-only slice.
