# Align Template Toolbars

## Prompt

Continue aligning Vue layout, style, and interaction behavior with FoolFrame,
allowing visual polish while preserving each old template's workflow, and
commit each behavior atomically.

## Scope

- Compare `view.jade`, `viewWithChart.jade`, and `Sudoku.jade` top-level
  controls with the shared Vue list panel.
- Keep search, report, and metadata create commands on normal list Views.
- Keep only search on chart Views and omit the invented Sudoku root toolbar.
- Close an open report when navigating between Views and prevent list report
  state from rendering on chart or Sudoku templates.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/ViewListPanel.vue`
- `frontend/src/payload.test.ts`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/12/2026-07-12T15-21-27Z-align-template-toolbars.md`

## Validation

- `cd frontend && npm test -- --run src/payload.test.ts`: 81 tests passed.
- `cd frontend && npm test -- --run && npm run build`: 152 tests passed and
  the production build completed.
- `docker compose build frontend && docker compose up -d --no-deps frontend`:
  final frontend image built and restarted; manifest
  `sha256:6904c4b434e1b14f34815c810c6124a997f0967fc5e79b960212238f4580d3f8`.
- `python scripts/runtime_doctor.py`: passed, including list/chart/Sudoku
  metadata and data routes plus auth, detail, report, and message checks.
- `python scripts/check_repo_harness.py`: passed.
- `docker compose ps -a`: frontend/backend/MySQL/Redis running; `db-migrate`
  exited successfully with code 0.
- `git diff --check`: passed.

## Source Evidence

- `view.jade` renders `查找`, `统计`, and non-selection View operations.
- `viewWithChart.jade` renders only `查找` before its data/chart tabs.
- `Sudoku.jade` starts directly with metadata-defined child panels and has no
  root search, report, or create toolbar.
- Vue now derives all three cases from the existing `viewTemplateKind` result;
  no business DTO or duplicate template-capability model was introduced.

## Risks And Follow-Ups

- Current-build authenticated desktop/mobile visual replay remains pending
  fresh authorization to read and fill the current local CAPTCHA.
