# Legacy Realtime Chart Window

## Prompt

Continue aligning old-page layout, style, and interaction behavior, committing
every change atomically and retaining View-first rendering and code reuse.

## Scope

- Restore the old Sudoku `linechart` detail-sample data source.
- Restore its fixed 100-point append-and-shift window and refresh interval.
- Keep list/map panels and top-level `viewWithChart` behavior unchanged.

## Changed Files

- `frontend/src/viewWorkflow.ts`
- `frontend/src/useSudokuPanels.ts`
- `frontend/src/SudokuPanels.vue`
- `frontend/src/viewWorkflow.test.ts`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T20-08-43Z-legacy-realtime-chart-window.md`

## Legacy Evidence

- `includes/linechart.jade` passes its child `viewid` to
  `LineChartController` instead of rendering a list result.
- `LineChartController.updateData` calls `/itemview` for that View and reads
  one `Data.SimpleData` sample per response.
- The first response fixes axis/series metadata, seeds 100 empty categories and
  100 zeroes per series, then every response appends one sample and shifts the
  oldest value when the window exceeds 100.
- The same detail response supplies `AutoFreshTime` for timer registration.

## Implementation

- Reused one View-first detail loader for Sudoku Item and linechart panels;
  linechart no longer requests a five-row list snapshot.
- Added a shared chart helper that initializes and advances the fixed rolling
  window while preserving first-sample axis and series metadata.
- Stored rolling chart state beside each child View result and bound only the
  compact linechart to it. List and map panels retain their existing list data.
- Scheduled linechart refresh from detail `AutoFreshTime`; other panels still
  use list `AutoFreshTime`.

## Validation

- Full frontend suite: 157 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- Frontend Docker build and forced recreation passed. Deployed image id:
  `sha256:0cb32674420a99aefa0307e1dd4fa0fb6c88e702e52139fb9c825abaf76976a0`.
- Backend `/test` passed; Compose checks retained healthy MySQL/Redis and
  `db-migrate` at `Exited (0)`.
- Authenticated `/view103` initially rendered 100 bars and 200 value labels;
  its tail was `1001 / 0.25 / 62500`. One scoped Price Chart refresh retained
  the same counts and produced two trailing `0.25 / 62500` samples.
- At 390x844 the chart pane was 328px wide, its SVG was 302px wide, and the
  document scroll width remained exactly 390px.
- Authenticated `/view100` retained its eight list-derived chart categories.

## Runtime Evidence

- `artifacts/runs/20260714-legacy-realtime-chart-window/price-chart-rolling-window-desktop.png`
- `artifacts/runs/20260714-legacy-realtime-chart-window/price-chart-rolling-window-mobile.png`

## Risks And Follow-Ups

- The legacy linechart footer and refresh-command availability still require a
  separate interaction-parity comparison; this slice changes only the data
  source, rolling window, and timer source.
- Complete page parity remains subject to further View-by-View comparison.
- The existing authenticated browser session was reused, so no fresh CAPTCHA
  was generated or read for this frontend-only slice.
