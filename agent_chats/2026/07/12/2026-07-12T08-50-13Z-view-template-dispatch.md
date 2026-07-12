# View Template Dispatch

## Prompt

- Continue aligning Vue layout, style, and interaction behavior with the old
  FoolFrame Web pages, using an atomic commit for each change.

## Scope

- Normalize top-level `TempFile` names with optional paths and `.jade` suffixes.
- Dispatch default `view`, `viewWithChart`, and `Sudoku` explicitly.
- Prevent unknown custom templates from querying and rendering as ordinary
  lists, details, or reports.
- Show an explicit migration state for unsupported custom templates.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/ViewListPanel.vue`
- `frontend/src/payload.test.ts`
- `frontend/src/viewWorkflow.ts`
- `frontend/src/viewWorkflow.test.ts`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/12/2026-07-12T08-50-13Z-view-template-dispatch.md`

## Validation

- `cd frontend && npm test && npm run build`
  - 137 tests passed.
  - Vue TypeScript checking and Vite production build passed.
- `docker compose build frontend && docker compose up -d --no-deps frontend`
  - Rebuilt frontend deployed on port 8081.
- `python scripts/runtime_doctor.py`
  - Full runtime checks passed, including configured chart and Sudoku template
    metadata and data paths.
- `python scripts/check_repo_harness.py`
  - Repository harness passed.
- `git diff --check` passed.

## Runtime Evidence

- The deployed Docker frontend served the rebuilt template-dispatch bundle.
- Runtime doctor proved the repository's configured `viewWithChart` and
  `Sudoku` paths remained healthy.

## Risks And Follow-Ups

- This migration does not execute arbitrary external custom Jade templates in
  Vue; unsupported names are intentionally visible and data-safe.
- Final authenticated browser acceptance must revisit list, chart, Sudoku, and
  an unsupported-template fixture if one is added.
