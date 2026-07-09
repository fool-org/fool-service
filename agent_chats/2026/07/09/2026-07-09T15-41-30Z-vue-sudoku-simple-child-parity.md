# Vue Sudoku Simple Child Parity

## Prompt

- Continue the Docker/Vue FoolFrame migration.
- Keep the frontend View-first and aligned with `../FoolFrame`.

## Scope

- Compared `../FoolFrame/src/Web/public/javascripts/app/groupview.js`.
- Matched the legacy `ListViewType=1` group child branch, which appends
  `这是简单项`, by rendering `简单项` in the Vue Sudoku group child panel.
- Kept the branch as a View metadata-driven placeholder instead of adding a
  new business DTO detail shortcut.

## Changed Files

- `frontend/src/SudokuPanels.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/09/2026-07-09T15-41-30Z-vue-sudoku-simple-child-parity.md`

## Validation

- `cd frontend && npm test`
- `cd frontend && npm run build`
- `docker compose build frontend`
- `docker compose up -d --no-deps --force-recreate frontend`
- Headless Chrome CDP smoke loaded `http://localhost:8081`, opened
  `ViewId=103`, and verified `Group Detail`, `简单项`, no `Simple item`, and
  no `.error` / `[role=alert]`.

## Artifacts

- `artifacts/runs/2026-07-09-sudoku-simple-child-smoke/sudoku-simple-child-dom.json`
- `artifacts/runs/2026-07-09-sudoku-simple-child-smoke/sudoku-simple-child-page.png`

## Risks

- This intentionally does not implement deeper simple-child data loading
  because the compared FoolFrame `groupview.js` branch is also a simple-item
  placeholder.
