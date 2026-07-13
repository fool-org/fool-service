# Legacy Chart Bar Corners

## Prompt

Continue aligning old-page layout, style, and interaction behavior, committing
every change atomically and retaining View-first rendering and code reuse.

## Scope

- Compare the shared SVG bar-corner presentation with old `swchartLine.js` and
  the exact ECharts 3.1.7 renderer used by FoolFrame.
- Remove the confirmed Vue-only radius without changing chart data projection,
  geometry, responsive sizing, or either chart entrypoint.
- Re-audit the previously deferred first/last bar clipping question before
  changing any endpoint behavior.

## Changed Files

- `frontend/src/LegacyChartPanel.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T21-39-32Z-legacy-chart-bar-corners.md`

## Legacy Evidence

- FoolFrame declares `echarts: ^3.1.7`, and both bar-series branches in
  `swchartLine.js` omit `itemStyle.normal.barBorderRadius`.
- ECharts 3.1.7 `BarView` sets its rectangle radius from configured
  `barBorderRadius || 0`; the old default is therefore square.
- The same old `BarView` adds bar rectangles directly to the chart group and
  applies no grid clip path. Its bar layout centers the first/last bars on the
  category endpoints, so the current half-width extension beyond the grid is
  already aligned and was intentionally left unchanged.

## Implementation

- Removed the fixed `rx="2"` from the existing shared SVG bar rectangle.
- Added a focused source-contract assertion preventing the radius from being
  reintroduced. No component, helper, dependency, business DTO path, or CSS
  rule was added.

## Validation

- Full frontend suite: 164 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- `python scripts/check_repo_harness.py` and `git diff --check` passed.
- Frontend Docker build and forced recreation passed. Deployed image id:
  `sha256:e05a1936abc0b5cb4d1854e16a42e99276f9e6fc4d8eda43cf951dbd5126058a`.
- Backend `/test` and Compose service checks passed; MySQL/Redis were healthy
  and `db-migrate` remained `Exited (0)`.
- Authenticated `/view100` rendered eight bars with no `rx`/`ry` attributes and
  retained the 15px rendered cap at 1440x1000 and 390x844.
- Authenticated `/view103` retained 100 compact realtime bar nodes with no
  rounded attributes at both viewports. Browser warnings/errors were empty,
  and neither page produced document-level horizontal overflow.

## Runtime Evidence

- `artifacts/runs/20260714-legacy-chart-bar-corners/order-chart-desktop.png`
- `artifacts/runs/20260714-legacy-chart-bar-corners/order-chart-mobile.png`
- `artifacts/runs/20260714-legacy-chart-bar-corners/sudoku-chart-desktop.png`
- `artifacts/runs/20260714-legacy-chart-bar-corners/sudoku-chart-mobile.png`

## Risks And Follow-Ups

- No current View metadata exposes a per-series bar-corner override. If a real
  migrated View later does, it should enter through the shared View projection
  rather than a concrete business DTO.
- The existing authenticated browser session was reused, so no fresh CAPTCHA
  was generated or read for this frontend-only slice.
