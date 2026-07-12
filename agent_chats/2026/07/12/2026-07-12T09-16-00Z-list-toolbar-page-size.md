# List Toolbar Page Size

## Prompt

- Continue aligning Vue layout, style, and interaction behavior with the old
  FoolFrame Web pages, using an atomic commit for each change.

## Scope

- Remove the Vue-only editable page-size control from the main View toolbar.
- Restore `view.jade`'s fixed 10-row request and paginator size.
- Keep page size as one App workflow ref passed read-only to the list panel.
- Replace the fixed toolbar grid with a wrapping layout for metadata-defined
  create operation counts and the report action.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/ViewListPanel.vue`
- `frontend/src/style.css`
- `frontend/src/payload.test.ts`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/12/2026-07-12T09-16-00Z-list-toolbar-page-size.md`

## Validation

- `cd frontend && npm test && npm run build`
  - 139 tests passed.
  - Vue TypeScript checking and Vite production build passed.
- `docker compose build frontend && docker compose up -d --no-deps frontend`
  - Rebuilt frontend deployed on port 8081.
- `python scripts/runtime_doctor.py`
  - Full runtime checks passed, including legacy 10-row list query paging.
- `python scripts/check_repo_harness.py`
  - Repository harness passed.
- `git diff --check` passed.

## Runtime Evidence

- Docker serves the rebuilt list toolbar at `http://localhost:8081`.
- Runtime doctor proved the View-first query and paging chain remains healthy
  with the restored fixed request size.

## Skipped Or Downgraded Checks

- Authenticated browser interaction remains gated by a fresh CAPTCHA without
  current solve permission.

## Risks And Follow-Ups

- Final authenticated desktop and 390x844 browser acceptance must prove the
  compact toolbar, metadata operation wrapping, 10-row page, and paginator.
