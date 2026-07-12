# Sudoku Panel Refresh

## Prompt

- Continue aligning Vue layout, style, and interaction behavior with the old
  FoolFrame Web pages, using an atomic commit for each change.

## Scope

- Show each Sudoku data panel's legacy `FreshTime`.
- Add targeted top-level and Group-child list refresh commands.
- Schedule per-panel `AutoFreshTime` without duplicating timers after metadata
  objects reload.
- Preserve shared ViewId list/detail data and clear timers on View changes or
  unmount.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/SudokuPanels.vue`
- `frontend/src/ViewListPanel.vue`
- `frontend/src/payload.test.ts`
- `frontend/src/style.css`
- `frontend/src/useSudokuPanels.ts`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/12/2026-07-12T08-34-36Z-sudoku-panel-refresh.md`

## Validation

- `cd frontend && npm test && npm run build`
  - 132 tests passed.
  - Vue TypeScript checking and Vite production build passed.
- `docker compose build frontend`
  - Frontend image built independently.
- `docker compose up -d --no-deps frontend`
  - Rebuilt frontend deployed on port 8081.
- `python scripts/runtime_doctor.py`
  - Full runtime checks passed, including shared-View Sudoku metadata and row /
    map / item data paths.
- `python scripts/check_repo_harness.py`
  - Repository harness passed.
- `git diff --check` passed.

## Runtime Evidence

- The deployed Docker frontend served the rebuilt Vue bundle.
- Runtime doctor proved Sudoku template, Group metadata, and shared child View
  data remained available after the refresh workflow change.

## Risks And Follow-Ups

- Final authenticated browser acceptance must click top-level and Group child
  refresh commands and inspect timestamps on desktop and mobile.
- The browser presents a fresh CAPTCHA without current solve permission, so no
  authenticated screenshot is claimed for this slice.
