# Detail Collection Tabs

## Prompt

- Continue aligning Vue layout, style, and interaction behavior with the old
  FoolFrame Web pages, using an atomic commit for each change.

## Scope

- Render each detail `Items[]` group as a metadata-keyed tab, matching
  `detailView.jade` collection navigation.
- Render dynamic child columns, rows, editors, and actions in a shared
  legacy-style table instead of one card per row.
- Keep group tabs scrollable and child tables horizontally scrollable for the
  390px mobile viewport.
- Preserve inline edits for `DetailViewId=0`, deep links for nonzero detail
  Views, and the existing select-from-existing workflow.

## Changed Files

- `frontend/src/ViewDetailPanel.vue`
- `frontend/src/style.css`
- `frontend/src/payload.test.ts`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/12/2026-07-12T09-09-00Z-detail-collection-tabs.md`

## Validation

- `cd frontend && npm test && npm run build`
  - 138 tests passed.
  - Vue TypeScript checking and Vite production build passed.
- `docker compose build frontend && docker compose up -d --no-deps frontend`
  - Rebuilt frontend deployed on port 8081.
- `python scripts/runtime_doctor.py`
  - Full runtime checks passed, including detail collection metadata and
    child update/add/delete paths.
- `python scripts/check_repo_harness.py`
  - Repository harness passed.
- `git diff --check` passed.

## Runtime Evidence

- Docker serves the rebuilt collection tabs and tables at
  `http://localhost:8081`.
- Runtime doctor proved the View-first child collection read/write chains
  remain healthy after the presentation change.

## Skipped Or Downgraded Checks

- Authenticated browser interaction remains gated by a fresh CAPTCHA without
  current solve permission.

## Risks And Follow-Ups

- Final authenticated desktop and 390x844 browser acceptance must prove tab
  switching, horizontal table scrolling, inline save/delete, and deep links.
- The select-from-existing candidate area remains inline and should be aligned
  with the old modal in a separate atomic slice.
