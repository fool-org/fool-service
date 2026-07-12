# List Action Copy

## Prompt

- Continue aligning Vue layout, style, and interaction behavior with the old
  FoolFrame Web pages, using an atomic commit for each change.

## Scope

- Restore `view.jade` toolbar order: query input, search, report, then
  metadata-defined create operations.
- Restore old Web Chinese labels for list query, report, operation column,
  empty states, paging summary, and refresh time.
- Use the old Chinese Select label in the shared child candidate table.
- Preserve all metadata-returned View titles, column names, and operation names
  without frontend translation or business DTO defaults.

## Changed Files

- `frontend/src/ViewListPanel.vue`
- `frontend/src/ListDataTable.vue`
- `frontend/src/ViewDetailPanel.vue`
- `frontend/src/payload.test.ts`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/12/2026-07-12T09-25-00Z-list-action-copy.md`

## Validation

- `cd frontend && npm test && npm run build`
  - 139 tests passed.
  - Vue TypeScript checking and Vite production build passed.
- `docker compose build frontend && docker compose up -d --no-deps frontend`
  - Rebuilt frontend deployed on port 8081.
- `python scripts/runtime_doctor.py`
  - Full runtime checks passed, including list metadata, query, paging, and
    operation routes.
- `python scripts/check_repo_harness.py`
  - Repository harness passed.
- `git diff --check` passed.

## Runtime Evidence

- Docker serves the rebuilt list copy/order at `http://localhost:8081`.
- Runtime doctor proved list metadata and data behavior remains healthy after
  the static presentation change.

## Skipped Or Downgraded Checks

- Authenticated browser interaction remains gated by a fresh CAPTCHA without
  current solve permission.

## Risks And Follow-Ups

- Final authenticated desktop and 390x844 browser acceptance must prove action
  order, metadata labels, table operation column, paging, and refresh text.
