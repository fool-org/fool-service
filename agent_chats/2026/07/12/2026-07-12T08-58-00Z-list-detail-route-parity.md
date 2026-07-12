# List And Detail Route Parity

## Prompt

- Continue aligning Vue layout, style, and interaction behavior with the old
  FoolFrame Web pages, using an atomic commit for each change.

## Scope

- Keep `view.jade` list pages list-only instead of selecting the first row and
  embedding a detail DTO beside the table.
- Route metadata-defined row and create operations through the old
  `/view{id}/{obj}` and `/new{id}` paths.
- Render detail controls only for standalone detail/new or metadata-only item
  routes, using the full workspace width.
- Allow standalone detail child writes from the loaded detail object id rather
  than requiring the object to remain in a list response.
- Return to the source page after a successful detail save, matching
  `detailview.js` history behavior.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/ViewListPanel.vue`
- `frontend/src/style.css`
- `frontend/src/viewWorkflow.ts`
- `frontend/src/viewWorkflow.test.ts`
- `frontend/src/payload.test.ts`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/12/2026-07-12T08-58-00Z-list-detail-route-parity.md`

## Validation

- `cd frontend && npm test && npm run build`
  - 137 tests passed.
  - Vue TypeScript checking and Vite production build passed.
- `docker compose build frontend && docker compose up -d --no-deps frontend`
  - Rebuilt frontend deployed on port 8081.
- `python scripts/runtime_doctor.py`
  - Full runtime checks passed, including list, detail, new, and owner-new Vue
    route fallbacks plus the View-first metadata/data/detail/save chains.
- `python scripts/check_repo_harness.py`
  - Repository harness passed.
- `git diff --check` passed.

## Runtime Evidence

- Docker serves the rebuilt frontend at `http://localhost:8081`.
- Runtime doctor proved `/view100`, `/view100/1001`, `/new100`, and
  `/new100/1001&100&items` all return the Vue entrypoint while the matching
  metadata, data, detail, init, and save APIs remain healthy.

## Skipped Or Downgraded Checks

- Authenticated browser interaction remains gated by a fresh CAPTCHA without
  current solve permission.

## Risks And Follow-Ups

- Final authenticated desktop and 390x844 browser acceptance must prove the
  list-only page, row/create navigation, standalone edit/save, and history
  return flow.
