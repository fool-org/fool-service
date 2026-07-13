# Legacy Chart Height Lock

## Prompt

Continue aligning old-page layout, style, and interaction behavior, committing
every change atomically and retaining View-first rendering and code reuse.

## Scope

- Compare the top-level chart tab's rendered height with old
  `viewWithChart.js` and the current data tab.
- Restore the old one-time lock without changing Sudoku's independent compact
  height, data projection, or query state.
- Keep category labels readable after the chart adopts the data pane's aspect
  ratio, reusing the existing responsive sampling branch.

## Changed Files

- `frontend/src/ViewListPanel.vue`
- `frontend/src/LegacyChartPanel.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T21-53-00Z-legacy-chart-height-lock.md`

## Legacy Evidence

- `viewWithChart.js` starts with `isInitChart=false`; on the first
  View-derived query result it assigns the chart tab's height from the data tab
  before initializing ECharts, then never reassigns it in later query updates.
- Pagination remains outside both old tab panes, so only the data surface is
  part of the measured height.
- ECharts category axes use automatic label intervals; the current shared
  chart already had a rendered-width sampler for compact labels.

## Implementation

- `ViewListPanel` now measures its existing `.view-table` after the first data
  render, rounds the browser height like the old jQuery path, and resets it only
  when View id, template, or navigation entry changes.
- The shared chart accepts that optional presentation height, fills it, and
  adjusts its viewBox width from the observed aspect ratio so text is not
  stretched. Compact charts continue to ignore the new prop.
- Reused the same label sampler for fixed-height and compact charts, basing its
  capacity on rendered plot width. No business DTO, duplicated renderer,
  dependency, composable, or API request was added.

## Validation

- Full frontend suite: 164 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- `python scripts/check_repo_harness.py` and `git diff --check` passed.
- Frontend Docker build and forced recreation passed. Deployed image id:
  `sha256:ac2c0bcedf8fbcf4340fa18d800cc36d8094436f51e4ed829a0e1db656d1d9d7`.
- Backend `/test` and Compose checks passed; MySQL/Redis were healthy and
  `db-migrate` remained `Exited (0)`.
- Before the change, authenticated `/view100` measured a 414px desktop data
  pane and a 566.66px chart. Afterward both measured 414px, and a Find request
  while the Chart tab was active retained that height and tab.
- At 390x844, the data pane measured 470.25px and the old-style integer chart
  lock measured 470px. The chart rendered only first/last labels with no
  overlap; desktop retained all eight with no overlap.
- Authenticated `/view103` retained its 328x200 compact chart and two mobile
  category labels. Browser warnings/errors were empty and document width
  matched the viewport at both sizes.

## Runtime Evidence

- `artifacts/runs/20260714-legacy-chart-height-lock/order-chart-desktop.png`
- `artifacts/runs/20260714-legacy-chart-height-lock/order-chart-mobile.png`

## Risks And Follow-Ups

- The locked pixel height intentionally survives viewport resize until the
  View page is entered again, matching the old controller's one-time behavior.
- The existing authenticated browser session was reused, so no fresh CAPTCHA
  was generated or read for this frontend-only slice.
