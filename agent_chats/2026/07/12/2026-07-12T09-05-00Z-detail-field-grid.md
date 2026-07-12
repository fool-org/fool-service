# Detail Field Grid

## Prompt

- Continue aligning Vue layout, style, and interaction behavior with the old
  FoolFrame Web pages, using an atomic commit for each change.

## Scope

- Reuse one metadata-driven simple-field grid for standalone detail read and
  edit states.
- Match `detailView.jade` desktop density with two field groups per row.
- Preserve a one-column field flow at the 390px mobile breakpoint.
- Keep child collection layout outside this change for a separate atomic
  migration slice.

## Changed Files

- `frontend/src/ViewDetailPanel.vue`
- `frontend/src/style.css`
- `frontend/src/payload.test.ts`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/12/2026-07-12T09-05-00Z-detail-field-grid.md`

## Validation

- `cd frontend && npm test && npm run build`
  - 137 tests passed.
  - Vue TypeScript checking and Vite production build passed.
- `docker compose build frontend && docker compose up -d --no-deps frontend`
  - Rebuilt frontend deployed on port 8081.
- `python scripts/runtime_doctor.py`
  - Full runtime checks passed, including detail metadata, lookup, enum, save,
    and child collection paths.
- `python scripts/check_repo_harness.py`
  - Repository harness passed.
- `git diff --check` passed.

## Runtime Evidence

- Docker serves the rebuilt detail field grid at `http://localhost:8081`.
- Runtime doctor proved the View-first detail and save chains remain healthy
  after the responsive layout change.

## Skipped Or Downgraded Checks

- Authenticated browser interaction remains gated by a fresh CAPTCHA without
  current solve permission.

## Risks And Follow-Ups

- Final authenticated desktop and 390x844 browser acceptance must prove field
  alignment, long values, lookup controls, edit/save state, and no overflow.
- Child collection tabs and table layout remain a separate parity slice.
