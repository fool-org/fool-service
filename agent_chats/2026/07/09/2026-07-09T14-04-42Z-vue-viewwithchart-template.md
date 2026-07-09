# Vue viewWithChart Template

# Prompt

Continue the Docker/FoolFrame/Vue migration, check legacy View rendering before
data binding, keep files small, and avoid concrete business DTO binding.

# Scope

- Legacy `TempFile=viewWithChart` rendering behavior from
  `../FoolFrame/src/Web/views/viewWithChart.jade`.
- Vue View workflow template selection and chart-data derivation.
- Migration task-state and delivery evidence for this frontend slice.

# Changes

- Added shared Vue helpers for legacy View template names and `viewWithChart`
  detection.
- Added `legacyChartData`, which derives chart labels/series only from
  FoolFrame row item `EditType` values `11`, `12`, `13`, and `14`.
- Rendered `viewWithChart` pages as legacy data/chart panes in `App.vue`.
- Used a native `<meter>`-based chart panel instead of adding a chart
  dependency.
- Updated `tasks.md` and `docs/migration/foolframe-parity.md`.

# Validation

- Red:
  `cd frontend && npm test -- viewWorkflow.test.ts`
  failed because `viewTemplateName` and `legacyChartData` did not exist.
- Red:
  `cd frontend && npm test -- payload.test.ts`
  failed because `App.vue` did not consume `viewUsesChartTemplate`.
- Green:
  `cd frontend && npm test -- viewWorkflow.test.ts payload.test.ts`
  passed `104` tests.
- Green:
  `cd frontend && npm test`
  passed `114` tests.
- Green:
  `cd frontend && npm run build`.
- Green:
  `python scripts/runtime_doctor.py`.

# Runtime Evidence

- Rebuilt and recreated the Docker frontend:
  `docker compose up -d --build frontend`
  then `docker compose up -d --no-deps --force-recreate frontend`.
- HTTP bundle check found `view-template-tabs`, `legacy-chart-pane`, and
  `viewWithChart` in the current `localhost:8081` bundle.
- In-app Browser check at `http://localhost:8081/` showed the default View
  with `数据` / `图表` tabs, no framework overlay, no console warnings/errors,
  and after clicking `图表` the `.legacy-chart-pane` was visible while the
  table pane was hidden.

# Risks

- Current Docker seed data has no chart item `EditType` values `11` through
  `14`, so the chart pane correctly shows an empty chart state. Backend enum
  and seed support for those legacy chart edit types should be a separate
  slice.

# Follow-ups

- Add backend/seed support for FoolFrame chart item edit types `11` to `14`
  before claiming `viewWithChart` chart output parity.
