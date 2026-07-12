# Legacy Chart Palette

## Prompt

- Continue aligning Vue layout, style, and interaction behavior with the old
  FoolFrame Web pages, using an atomic commit for each change.

## Scope

- Compare `LegacyChartPanel.vue` with old `swchartLine.js` and its ECharts
  configuration.
- Replace the invented Indigo-heavy palette with the legacy ECharts default
  color order used when the old code omitted an explicit `color` option.
- Keep legend text bound only to View-derived series names; remove the extra
  English series type label.
- Use Chinese fallback series and chart accessibility labels.

## Changed Files

- `frontend/src/LegacyChartPanel.vue`
- `frontend/src/payload.test.ts`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/12/2026-07-12T10-01-32Z-legacy-chart-palette.md`

## Validation

- `cd frontend && npm test -- --run`
  - 141 tests passed across 8 files.
- `cd frontend && npm run build`
  - Vue TypeScript checking and Vite production build passed.
- `python scripts/check_repo_harness.py` passed.

## Runtime Evidence

- `docker compose build frontend && docker compose up -d --no-deps frontend`
  rebuilt and restarted the Vue frontend successfully.
- `python scripts/runtime_doctor.py` passed all Compose, auth, View, data,
  detail, child, report, message, chart, and Sudoku checks, including seeded
  chart `EditType` row data.

## Skipped Or Downgraded Checks

- Authenticated chart interaction remains gated by the current captcha until
  fresh user authorization is received.

## Risks And Follow-Ups

- Browser acceptance must prove chart series/legend colors, tooltips, label
  density, and responsive framing on top-level and Sudoku chart panels.
