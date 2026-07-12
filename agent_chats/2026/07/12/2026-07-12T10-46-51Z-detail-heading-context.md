# Detail Heading Context

## Prompt

- Continue aligning Vue layout, style, and interaction behavior with the old
  FoolFrame Web pages, using an atomic commit for each change.

## Scope

- Compare the detail heading with old `detailView.jade`.
- Render object IDs inline as `视图名 -对象ID`.
- Render create routes as `视图名 -新建`.
- Keep schema-only detail titles free of object context.
- Remove the invented rounded Tag and localize the fallback title.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/ViewDetailPanel.vue`
- `frontend/src/payload.test.ts`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/12/2026-07-12T10-46-51Z-detail-heading-context.md`

## Validation

- `cd frontend && npm test -- --run`
  - 146 tests passed across 9 files.
- `cd frontend && npm run build`
  - Vue TypeScript checking and Vite production build passed.
  - Removing PrimeVue Tag reduced the detail chunk from 74.03 kB to 70.76 kB.
- `python scripts/check_repo_harness.py` passed.

## Runtime Evidence

- `docker compose build frontend` rebuilt the Vue image successfully.
- `docker compose up -d --no-deps frontend` recreated the frontend at
  `http://localhost:8081`.
- `python scripts/runtime_doctor.py` passed all Compose, auth, View, data,
  detail, child, report, message, chart, and Sudoku checks.
- `docker compose ps -a` shows frontend/backend running, MySQL/Redis healthy,
  and `db-migrate` at `Exited (0)`.

## Skipped Or Downgraded Checks

- Authenticated detail interaction requires a fresh captcha authorization.

## Risks And Follow-Ups

- Browser acceptance must prove existing, create, and schema-only title states
  at desktop and narrow widths.
