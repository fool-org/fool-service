# Shared SVG Chart

## Prompt
- Continue the FoolFrame-to-Vue migration with a usable frontend, maximum
  metadata reuse, controlled file size, and atomic commits.

## Scope
- Compared old ECharts-backed `viewWithChart.js` / `swchartLine.js` with the
  current Vue `<meter>` rows.
- Added one responsive `LegacyChartPanel.vue` for top-level `viewWithChart` and
  Sudoku `linechart` panels.
- Reused the existing `legacyChartData` projection from View row `Items` and
  `EditType` values `11` through `14`.
- Rendered line, bar, and scatter series with axes, five value ticks, a zero
  baseline, sampled category labels, point/bar tooltips, and a shared legend.
- Kept the implementation on native SVG rather than adding another chart
  dependency after the map-specific Leaflet addition.
- Removed duplicated chart maximum calculations and meter templates from
  `App.vue` and `SudokuPanels.vue`.

## Changed Files
- `frontend/src/LegacyChartPanel.vue`
- `frontend/src/App.vue`
- `frontend/src/SudokuPanels.vue`
- `frontend/src/payload.test.ts`
- `frontend/src/style.css`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/10/2026-07-10T05-42-02Z-shared-svg-chart.md`

## Validation
- `cd frontend && npm test -- --run` passed: 7 files, 130 tests.
- `cd frontend && npm run build` passed.
- `python scripts/check_repo_harness.py` passed.
- `git diff --check` passed.
- `docker compose build frontend` passed.
- `docker compose up -d --force-recreate --no-deps frontend` replaced the
  running frontend.
- `python scripts/runtime_doctor.py` passed all checks, including chart
  `EditType` row items through the Vue proxy.

## Runtime Evidence
- The main JavaScript is 140.20 kB gzip 48.22 kB; no chart dependency or chart
  chunk was added.
- `LegacyChartPanel.vue` is 125 lines.
- `App.vue` dropped from 1122 to 1108 lines and `SudokuPanels.vue` from 126 to
  115 lines.

## Risks
- Signed-in desktop/mobile pixel inspection and SVG tooltip interaction still
  require approval to submit the local captcha-backed Docker admin login.

## Follow-ups
- Complete signed-in chart/map/browser inspection after captcha approval.
- Continue source parity audit for other user-visible downgrades before
  claiming migration completion.
