# Legacy Chart Legend Toggle

## Prompt

Continue aligning old-page layout, style, and interaction behavior, committing
every change atomically and retaining View-first rendering and code reuse.

## Scope

- Restore the old ECharts legend click interaction in the shared Vue chart.
- Verify selection filtering in top-level, mobile, and compact chart surfaces.

## Changed Files

- `frontend/src/LegacyChartPanel.vue`
- `frontend/src/style.css`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T19-47-03Z-legacy-chart-legend-toggle.md`

## Legacy Evidence

- The old repository declares ECharts `^3.1.7`; `swchartLine.js` does not
  disable legend selection.
- ECharts 3.1.7's `LegendModel` defaults `selectedMode` to `true`, its
  `LegendView` dispatches selection on item click, and `legendFilter` excludes
  unselected series from the active series set.
- Selection keys are series names, and the old inactive legend color is
  `#ccc`.

## Implementation

- Added one name-based hidden-series state to `LegacyChartPanel.vue` and one
  computed visible-series list.
- Reused that list for y-axis domain and bar-series grouping while hiding the
  corresponding SVG series group. With every series hidden, the existing empty
  domain fallback remains `0..1`.
- Converted legend entries to native buttons with `aria-pressed`, keyboard
  focus treatment, and the old `#ccc` inactive swatch/text color.
- Added no request, View projection, chart dependency, component, or business
  DTO branch; both chart entrypoints retain the shared renderer.

## Validation

- Full frontend suite: 156 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- Frontend Docker build and forced recreation passed. Deployed image id:
  `sha256:a36732ca0e760ff9637c916c736760f3e4e0ea6fabec59b9dc46684071abb57e`.
- Backend `/test`, frontend `/view100`, and Compose service checks passed;
  MySQL/Redis were healthy and `db-migrate` remained `Exited (0)`.
- On authenticated desktop `/view100`, hiding Price changed its pressed state
  to false and its text/swatch to `rgb(204, 204, 204)`, removed the line, kept
  eight bars, and recomputed the top y tick from `62,500` to `1.5`.
- Hiding both series left zero visible series and the `1, 0.75, 0.5, 0.25, 0`
  fallback ticks. Enabling only Price restored the `62,500` top tick and one
  line with zero visible bars; enabling Amount restored both pressed states.
- At 390x844, the same Price toggle retained the column legend, inactive color,
  `1.5` top tick, and zero visible lines in a 304px chart.
- Authenticated `/view103` retained its 200px compact pane and five bar nodes;
  hiding Amount set the shared series displays to `none`/`inline` and left
  Price selected.

## Runtime Evidence

- `artifacts/runs/20260714-legacy-chart-legend-toggle/order-chart-price-hidden-desktop.png`
- `artifacts/runs/20260714-legacy-chart-legend-toggle/order-chart-price-hidden-mobile.png`

## Risks And Follow-Ups

- This slice restores legend selection only; complete chart and page parity
  remains subject to further comparison with the old View-driven workflows.
- The existing authenticated browser session was reused, so no fresh CAPTCHA
  was generated or read for this frontend-only slice.
