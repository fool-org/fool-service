# Vue Sudoku Map Panels

## Prompt

- Continue the FoolFrame migration while keeping the focus on View-first
  rendering, data loaded after View metadata, Vue frontend parity, file-size
  control, and code reuse.

## Scope

- Added the next narrow Vue Sudoku panel slice for legacy `Map` includes.
- Reused the loaded child `ListViewId` panel data path from the previous
  Sudoku work.
- Kept marker projection on legacy row `Items` metadata instead of binding to
  concrete business DTO fields.

## Changed Files

- `frontend/src/viewWorkflow.ts`
- `frontend/src/viewWorkflow.test.ts`
- `frontend/src/App.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/09/2026-07-09T14-58-06Z-vue-sudoku-map-panels.md`

## Behavior

- `legacyMapMarkers` projects markers from child `querydata` rows:
  - `EditType=16`: longitude
  - `EditType=17`: latitude
  - `EditType=18`: title
  - other row items: marker info text
- Sudoku `Map` panels render as a marker table when marker rows are present.
- No Baidu/third-party map SDK was added for this slice.
- Unsupported Sudoku `Group` and `Item` panels remain explicit empty states.

## Red Check

- `cd frontend && npm test -- viewWorkflow.test.ts payload.test.ts`
  - Failed before implementation because `legacyMapMarkers` did not exist and
    Sudoku map panels had no marker branch.

## Validation

- `cd frontend && npm test -- viewWorkflow.test.ts payload.test.ts`
- `cd frontend && npm test`
- `cd frontend && npm run build`
- `python scripts/check_repo_harness.py`
- `git diff --check`
- `docker compose up -d --build frontend`
- `docker compose up -d --no-deps --force-recreate frontend && docker compose ps && python scripts/runtime_doctor.py`

## Risks

- This slice renders map data as a deterministic marker table, not an
  interactive map. That keeps the migrated page useful without adding an
  unproven SDK dependency.
- Runtime doctor covers the stack and View-first route; the map-specific
  behavior is pinned by focused frontend tests.

## Follow-ups

- Continue Sudoku `Group` and `Item` include parity from `../FoolFrame` only
  after checking the legacy include render/data behavior.
