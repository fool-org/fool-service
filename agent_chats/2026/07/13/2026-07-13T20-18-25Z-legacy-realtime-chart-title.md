# Legacy Realtime Chart Title

## Prompt

Continue aligning old-page layout, style, and interaction behavior, committing
every change atomically and retaining View-first rendering and code reuse.

## Scope

- Restore the compact realtime chart's internal View-derived title.
- Keep top-level `viewWithChart` presentation unchanged.

## Changed Files

- `frontend/src/LegacyChartPanel.vue`
- `frontend/src/SudokuPanels.vue`
- `frontend/src/style.css`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T20-18-25Z-legacy-realtime-chart-title.md`

## Legacy Evidence

- `LineChartController` initializes ECharts `title.show=true` and sets
  `title.text` from `$scope.chartname`.
- `includes/linechart.jade` initializes `chartname` from the child panel's
  `item.Name`, so the title is View metadata rather than detail DTO content.
- The separate top-level `getchartoption` branch sets title text to a blank
  string and therefore must not receive the compact panel title.

## Implementation

- Added an optional title to the existing shared chart and rendered it only
  when compact mode and a View title are both present.
- Passed the existing `fieldTitle(panel)` metadata from Sudoku and reserved
  vertical plot space below the title.
- Reused the shared component and existing metadata helper; no business field,
  chart dependency, or second renderer was added.

## Validation

- Full frontend suite: 157 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- Frontend Docker build and forced recreation passed. Deployed image id:
  `sha256:faffef99d0c91fd7fdbe863971f49890087bd98f5567a620293d09cb28227ad9`.
- Authenticated `/view103` rendered exactly one inner `Price Chart` title; its
  bounding box ended above the first plot grid line and the compact pane stayed
  exactly 200px high.
- At 390x844 the compact pane remained 328px wide, the title stayed above the
  grid, and document scroll width remained exactly 390px.
- Authenticated `/view100` retained its eight-category top-level chart without
  an inner title in the accessibility snapshot.

## Runtime Evidence

- `artifacts/runs/20260714-legacy-realtime-chart-title/realtime-chart-title-desktop.png`
- `artifacts/runs/20260714-legacy-realtime-chart-title/realtime-chart-title-mobile.png`

## Risks And Follow-Ups

- Realtime series stacking is a separate old option and remains for its own
  coordinate-behavior comparison.
- Complete page parity remains subject to further View-by-View comparison.
- The existing authenticated browser session was reused, so no fresh CAPTCHA
  was generated or read for this frontend-only slice.
