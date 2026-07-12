# Single View Heading

## Prompt

- Continue aligning Vue layout, style, and interaction behavior with the old
  FoolFrame Web pages, using an atomic commit for each change.

## Scope

- Compare current list/detail heading ownership with old `view.jade` and
  `detailView.jade`.
- Remove duplicate workspace topbar titles and the visible protocol ViewName.
- Keep the business View title in the list/detail panel; leave the mobile
  topbar responsible only for opening the navigation Drawer.
- Delete unused `loadedViewName` / `viewTitle` computed state and title CSS
  instead of retaining hidden duplicate presentation state.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/ViewListPanel.vue`
- `frontend/src/useViewDataWorkflow.ts`
- `frontend/src/style.css`
- `frontend/src/payload.test.ts`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/12/2026-07-12T10-04-54Z-single-view-heading.md`

## Validation

- `cd frontend && npm test -- --run`
  - 142 tests passed across 8 files.
- `cd frontend && npm run build`
  - Vue TypeScript checking and Vite production build passed.
- `python scripts/check_repo_harness.py` passed.

## Runtime Evidence

- `docker compose build frontend && docker compose up -d --no-deps frontend`
  rebuilt and restarted the Vue frontend successfully.
- `python scripts/runtime_doctor.py` passed all Compose, auth, View, data,
  detail, child, report, message, chart, and Sudoku checks.

## Skipped Or Downgraded Checks

- Authenticated browser interaction remains gated by the current captcha until
  fresh user authorization is received.

## Risks And Follow-Ups

- Browser acceptance must prove one visible title on list/detail/new pages and
  a usable Drawer command with no empty mobile topbar space.
